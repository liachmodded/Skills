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
package org.inspirenxe.skills.impl.content.type.effect.potion;

import com.almuradev.droplet.content.inject.ChildModule;
import com.google.inject.TypeLiteral;
import org.inspirenxe.skills.impl.content.type.effect.ContentEffectType;
import org.inspirenxe.skills.impl.content.type.effect.potion.processor.AmplifierProcessor;
import org.inspirenxe.skills.impl.content.type.effect.potion.processor.DurationProcessor;
import org.inspirenxe.skills.impl.content.type.effect.potion.processor.IsAmbientProccessor;
import org.inspirenxe.skills.impl.content.type.effect.potion.processor.PotionProcessor;
import org.inspirenxe.skills.impl.content.type.effect.potion.processor.ShowParticlesProcessor;

public final class PotionEffectTypeModule extends ChildModule.Impl<ContentEffectType.Child> {

  @Override
  protected void configure0() {
    this.bindChildType(new ContentEffectType.Child("potion"));
    this.bindChildLoader(new TypeLiteral<PotionEffectTypeRootLoader>() {
    });

    this.bindBuilder(ContentPotionEffectTypeBuilder.class).to(ContentPotionEffectTypeBuilderImpl.class);

    this.bindProcessor(AmplifierProcessor.class);
    this.bindProcessor(DurationProcessor.class);
    this.bindProcessor(IsAmbientProccessor.class);
    this.bindProcessor(PotionProcessor.class);
    this.bindProcessor(ShowParticlesProcessor.class);

    this.bindFacet().toProvider(this.getProvider(PotionEffectTypeRootLoader.class));
  }
}
