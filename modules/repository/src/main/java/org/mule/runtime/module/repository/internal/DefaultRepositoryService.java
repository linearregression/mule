/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.repository.internal;

import static org.mule.runtime.module.repository.internal.RepositoryServiceFactory.MULE_REMOTE_REPOSITORIES_PROPERTY;
import org.mule.runtime.module.repository.api.BundleDescriptor;
import org.mule.runtime.module.repository.api.BundleNotFoundException;
import org.mule.runtime.module.repository.api.RepositoryConnectionException;
import org.mule.runtime.module.repository.api.RepositoryService;

import java.io.File;
import java.util.List;

import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.transfer.ArtifactNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation for {@code RepositoryService}.
 *
 * @since 4.0
 */
public class DefaultRepositoryService implements RepositoryService {

  private static final Logger logger = LoggerFactory.getLogger(DefaultRepositoryService.class);

  private final RepositorySystem repositorySystem;
  private final DefaultRepositorySystemSession repositorySystemSession;
  private final List<RemoteRepository> remoteRepositories;

  DefaultRepositoryService(RepositorySystem repositorySystem, DefaultRepositorySystemSession repositorySystemSession,
                           List<RemoteRepository> remoteRepositories) {
    this.repositorySystem = repositorySystem;
    this.repositorySystemSession = repositorySystemSession;
    this.remoteRepositories = remoteRepositories;
  }

  @Override
  public File lookupBundle(BundleDescriptor bundleDescriptor) {
    try {
      ArtifactRequest getArtifactRequest = new ArtifactRequest();
      if (remoteRepositories.isEmpty()) {
        logger.warn(
                    "Repository service has not been configured with remote repositories, therefore bundle resolution will work offline. "
                        + "In order to enabling accessing remote repositories for downloading missing bundles the set of repositories to use should set by using the system property: "
                        + MULE_REMOTE_REPOSITORIES_PROPERTY);
      } else {
        getArtifactRequest.setRepositories(remoteRepositories);
      }
      DefaultArtifact artifact = toArtifact(bundleDescriptor);
      getArtifactRequest.setArtifact(artifact);
      ArtifactResult artifactResult = repositorySystem.resolveArtifact(repositorySystemSession, getArtifactRequest);
      return artifactResult.getArtifact().getFile();
    } catch (ArtifactResolutionException e) {
      if (e.getCause() instanceof ArtifactNotFoundException) {
        throw new BundleNotFoundException("Couldn't find bundle " + bundleDescriptor.toString(), e);
      } else {
        throw new RepositoryConnectionException("There was a problem connecting to one of the repositories", e);

      }
    }
  }

  private DefaultArtifact toArtifact(BundleDescriptor bundleDescriptor) {
    return new DefaultArtifact(bundleDescriptor.getGroupId(), bundleDescriptor.getArtifactId(), bundleDescriptor.getType(),
                               bundleDescriptor.getVersion());
  }
}
