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
package org.inspirenxe.skills.impl.content.type.function;

import com.almuradev.droplet.content.inject.ChildModule;
import com.almuradev.droplet.content.inject.ForChild;
import com.almuradev.droplet.content.inject.RootModule;
import com.almuradev.droplet.content.processor.Processor;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import org.inspirenxe.skills.impl.content.type.function.economy.EconomyFunctionModule;
import org.inspirenxe.skills.impl.content.type.function.level.LevelFunctionModule;
import org.inspirenxe.skills.impl.content.type.function.processor.FormulaProcessor;

public final class FunctionModule extends RootModule.Impl<ContentFunction.Child, ContentFunctionBuilder<?>> {

  @Override
  protected void configure0() {
    this.bindRootType(new ContentFunction.Root());
    this.bindRootLoader(new TypeLiteral<FunctionRootLoader>() {
    });

    this.installChild(new EconomyFunctionModule(), new BaseChildModule());
    this.installChild(new LevelFunctionModule(), new BaseChildModule());

    this.bindProcessor(FormulaProcessor.class);
  }

  private static class BaseChildModule extends ChildModule.Impl<ContentFunction.Child> {

    @Override
    protected void configure0() {
      this.inSet(Key.get(Processor.class, ForChild.class));
    }
  }
}
