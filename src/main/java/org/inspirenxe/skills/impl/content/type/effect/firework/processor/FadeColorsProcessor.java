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
package org.inspirenxe.skills.impl.content.type.effect.firework.processor;

import com.almuradev.droplet.content.processor.Processor;
import com.almuradev.droplet.registry.Registry;
import com.almuradev.droplet.registry.RegistryKey;
import com.almuradev.droplet.registry.reference.RegistryReference;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.MoreCollectors;
import com.google.inject.Inject;
import net.kyori.xml.node.Node;
import net.kyori.xml.node.parser.Parser;
import org.inspirenxe.skills.api.color.ColorType;
import org.inspirenxe.skills.impl.content.type.effect.firework.ContentFireworkEffectTypeBuilder;

public final class FadeColorsProcessor implements Processor<ContentFireworkEffectTypeBuilder> {

  private final Registry<ColorType> registry;
  private final Parser<RegistryKey> registryKeyParser;

  @Inject
  public FadeColorsProcessor(final Registry<ColorType> registry, final Parser<RegistryKey> registryKeyParser) {
    this.registry = registry;
    this.registryKeyParser = registryKeyParser;
  }

  @Override
  public void process(final Node node, final ContentFireworkEffectTypeBuilder builder) {
    node.nodes("fade-colors").collect(MoreCollectors.toOptional()).ifPresent(fadeColors -> {
      final ImmutableList.Builder<RegistryReference<ColorType>> fadeColorsList = ImmutableList.builder();

      node.nodes("color").forEach(color -> fadeColorsList.add(this.registry.ref(this.registryKeyParser.parse(color))));
      builder.fadeColors(fadeColorsList.build());
    });
  }
}
