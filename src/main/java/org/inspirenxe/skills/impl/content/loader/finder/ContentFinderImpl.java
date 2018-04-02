/*
 * This file is part of Skills, licensed under the MIT License (MIT).
 *
 * Copyright (c) InspireNXE
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.inspirenxe.skills.impl.content.loader.finder;

import com.almuradev.droplet.content.configuration.ContentConfiguration;
import com.almuradev.droplet.content.loader.ChildContentLoader;
import com.almuradev.droplet.content.loader.finder.ContentFinder;
import com.almuradev.droplet.content.loader.finder.ContentVisitor;
import com.almuradev.droplet.content.loader.finder.FoundContent;
import com.almuradev.droplet.content.type.ContentType;
import com.almuradev.droplet.util.Logging;
import com.almuradev.droplet.util.PathVisitor;
import net.kyori.lunar.exception.Exceptions;
import org.slf4j.Logger;

import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class ContentFinderImpl implements ContentFinder {

  private final Logger logger;
  private final ContentConfiguration configuration;

  @Inject
  private ContentFinderImpl(final Logger logger, final ContentConfiguration configuration) {
    this.logger = logger;
    this.configuration = configuration;
  }

  @Override
  public <C extends ContentType.Child, R extends ContentType.Root<C>, V extends ContentVisitor<R, C>> FoundContent<R, C> find(final V visitor,
      final R rootType, final Set<ChildContentLoader<C>> childrenTypes) {
    this.logger.debug("{}Discovering {} content...", Logging.indent(1), rootType.id());
    this.configuration.searchPaths().forEach(Exceptions.rethrowConsumer(path -> {
      visitor.visitCore(path);
      visitor.visitNamespace(path);
      visitor.visitContent(path);
      final Path root = rootType.path(path).toAbsolutePath();
      visitor.visitRoot(rootType, root);
      childrenTypes.forEach(Exceptions.rethrowConsumer(child -> {
        ContentFinderImpl.this.logger.debug("{}Discovering {} content...", Logging.indent(2), child.type().id());
        final Path childPath = child.type().path(root).toAbsolutePath();
        visitor.visitChild(child, child.type(), childPath);
        Files.walkFileTree(childPath, Collections.emptySet(), this.configuration.maxDepth(), new PathVisitor() {
          @Override
          public FileVisitResult visitFile(final Path file, final BasicFileAttributes attributes) {
            ContentFinderImpl.this.logger.debug("{}Found {}", Logging.indent(3), childPath.relativize(file));
            visitor.visitEntry(file, child.builder());
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult preVisitDirectory(final Path directory, final BasicFileAttributes attributes) {
            if (directory.getFileName().toString().equals("include")) {
              ContentFinderImpl.this.logger.debug("{}Skipping include directory", Logging.indent(4));
              return FileVisitResult.SKIP_SUBTREE;
            }
            return FileVisitResult.CONTINUE;
          }
        });
      }));
    }));
    return visitor.foundContent();
  }
}