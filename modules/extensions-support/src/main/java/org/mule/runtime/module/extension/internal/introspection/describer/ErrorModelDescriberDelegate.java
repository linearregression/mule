package org.mule.runtime.module.extension.internal.introspection.describer;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.mule.runtime.dsl.api.component.ComponentIdentifier.parseComponentIdentifier;
import static org.mule.runtime.module.extension.internal.introspection.describer.AnnotationsBasedDescriber.MULE;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jgrapht.Graph;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.mule.runtime.api.meta.model.DefaultErrorModel;
import org.mule.runtime.api.meta.model.ErrorModel;
import org.mule.runtime.core.exception.ErrorTypeRepository;
import org.mule.runtime.core.exception.ErrorTypeRepositoryFactory;
import org.mule.runtime.extension.api.annotation.error.ErrorType;
import org.mule.runtime.extension.api.annotation.error.ErrorTypes;
import org.mule.runtime.extension.api.annotation.error.ExceptionMapping;
import org.mule.runtime.extension.api.annotation.error.ExceptionMappings;
import org.mule.runtime.extension.api.exception.IllegalModelDefinitionException;
import org.mule.runtime.module.extension.internal.introspection.describer.model.ExtensionElement;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ErrorModelDescriberDelegate {

    private final ErrorTypeRepository errorTypeRepository = ErrorTypeRepositoryFactory.createDefaultErrorTypeRepository();
    public static final DefaultErrorModel MULE_CONNECTIVITY_ERROR = new DefaultErrorModel("CONNECTIVITY", MULE, null, emptySet());
    public static final DefaultErrorModel EXTENSION_CONNECTIVITY_ERROR = new DefaultErrorModel("CONNECTIVITY", null, null, emptySet());
    public static final String ANY = "ANY";

    private ExtensionElement extensionElement;

    public ErrorModelDescriberDelegate(ExtensionElement extensionElement) {
        this.extensionElement = extensionElement;
    }

    public Map<String, ErrorModel> getErrorModels() {
        final DefaultDirectedGraph<String, Pair<String, String>> graph = new DefaultDirectedWeightedGraph<>(ImmutablePair::new);
        Optional<ErrorTypes> optionalErrorTypes = extensionElement.getAnnotation(ErrorTypes.class);
        optionalErrorTypes.ifPresent(annotation -> Stream.of(annotation.value()).forEach(errorType -> addType(errorType, graph)));

        extensionElement.getAnnotation(ErrorType.class).ifPresent(errorType -> addType(errorType, graph));
        graph.vertexSet().stream()
                .filter(errorType -> !this.isMuleError(errorType))
                .forEach(errorType -> {
                    if(graph.incomingEdgesOf(errorType).isEmpty()) {
                        throw new IllegalModelDefinitionException(String.format("The type %s is not defined", errorType));
                    }
                });

        CycleDetector<String, Pair<String, String>> cycleDetector = new CycleDetector<>(graph);
        if (cycleDetector.detectCycles()) {
            throw new IllegalModelDefinitionException("Cycle detected: " + cycleDetector.findCycles());
        }

        return toErrorModels(graph);
    }

    private Map<String, ErrorModel> toErrorModels(DefaultDirectedGraph<String, Pair<String, String>> graph) {
        Map<String, ErrorModel> errorModels = new HashMap<>();
        Map<String, Collection<Class<? extends Exception>>> errorTypeMapping = getErrorTypeMapping();
        if(!graph.vertexSet().isEmpty()){
            Map<String, String> inheritanceMap = graph.edgeSet().stream().collect(Collectors.toMap(Pair::getValue, Pair::getKey));

            ErrorModel MULE_ANY_ERROR_MODEL = DefaultErrorModel.builder(ANY).withNamespace(MULE).build();
            errorModels.put(ANY , MULE_ANY_ERROR_MODEL);
            graph.vertexSet()
                    .stream()
                    .filter(errorType -> graph.inDegreeOf(errorType) == 0)
                    .filter(errorType -> !errorType.equals(MULE_ANY_ERROR_MODEL.getType()))
                    .map(errorType -> DefaultErrorModel
                            .builder(errorType)
                            .withParent(
                                    DefaultErrorModel
                                            .builder(errorType)
                                            .withNamespace(MULE)
                                            .withParent(MULE_ANY_ERROR_MODEL)
                                            .build())
                            .build())
                    .forEach(error -> errorModels.put(error.getType(), error));

            new TopologicalOrderIterator<>(graph).forEachRemaining(errorType -> {
                if(!errorModels.containsKey(errorType)){

                    ErrorModel errorModel = DefaultErrorModel
                            .builder(errorType)
                            .withParent(errorModels.get(inheritanceMap.get(errorType)))
                            .withModelProperty(new ImplementingExceptionModelProperty(errorTypeMapping.getOrDefault(errorType, emptySet()).stream().collect(toSet())))
                            .build();
                    errorModels.put(errorType, errorModel);
                }
            });
        }
        return errorModels;
    }

    private boolean isMuleError(String errorIdentifier) {
        return errorTypeRepository.lookupOptErrorType(parseComponentIdentifier("MULE:"+errorIdentifier)).isPresent();
    }

    private void addType(ErrorType errorType, Graph<String, Pair<String, String>> graph) {
        graph.addVertex(errorType.value());
        graph.addVertex(errorType.parent());
        graph.addEdge(errorType.parent(), errorType.value());
    }

    public Map<Class<? extends Exception>, String> getExceptionErrorTypeMapping(){
        Map<Class<? extends Exception>, String> stringHashMap = new HashMap<>();
        getExceptionMapping(mapping -> stringHashMap.put(mapping.exceptionClass(), mapping.errorType()));
        return stringHashMap;
    }

    private Map<String, Collection<Class<? extends Exception>>> getErrorTypeMapping() {
        ListMultimap<String, Class<? extends Exception>> multimap = ArrayListMultimap.create();
        getExceptionMapping(mapping -> multimap.put(mapping.errorType(), mapping.exceptionClass()));
        return multimap.asMap();
    }

    private void getExceptionMapping(MapBuilder mapBuilder) {
        Optional<ExceptionMappings> optionalExceptionMappings = extensionElement.getAnnotation(ExceptionMappings.class);
        optionalExceptionMappings.ifPresent(annotation -> Stream.of(annotation.value()).forEach(mapBuilder::addMapping));
        extensionElement.getAnnotation(ExceptionMapping.class).ifPresent(mapBuilder::addMapping);
    }

    private interface MapBuilder {
        void addMapping(ExceptionMapping mapping);
    }
}
