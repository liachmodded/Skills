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
package org.inspirenxe.skills.impl.content.component.apply;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import net.kyori.feature.parser.FeatureDefinitionParserBinder;
import net.kyori.xml.node.parser.Parser;
import net.kyori.xml.node.parser.ParserBinder;
import org.inspirenxe.skills.impl.content.component.apply.cancel.EventCancelApplicator;
import org.inspirenxe.skills.impl.content.component.apply.cancel.EventCancelParser;
import org.inspirenxe.skills.impl.content.component.apply.cancel.TransactionCancelParser;
import org.inspirenxe.skills.impl.content.component.apply.data.DataApplicatorParser;
import org.inspirenxe.skills.impl.content.component.apply.economy.EconomyApplicatorParser;
import org.inspirenxe.skills.impl.content.component.apply.experience.ExperienceApplicatorParser;
import org.inspirenxe.skills.impl.content.component.apply.message.MessageApplicatorParser;

public class EventApplicatorModule extends AbstractModule {

    @Override
    protected void configure() {

        final ParserBinder parsers = new ParserBinder(this.binder());


        final MapBinder<String, Parser<? extends EventApplicator>> applicators = MapBinder.newMapBinder(this.binder(), new TypeLiteral<String>() {}, new TypeLiteral<Parser<? extends EventApplicator>>() {});

        applicators.addBinding("cancel-event").to(EventCancelParser.class);
        applicators.addBinding("cancel-transaction").to(TransactionCancelParser.class);
        applicators.addBinding("experience").to(ExperienceApplicatorParser.class);
        applicators.addBinding("economy-modifier").to(EconomyApplicatorParser.class);
        applicators.addBinding("message").to(MessageApplicatorParser.class);
        applicators.addBinding("data").to(DataApplicatorParser.class);


        parsers.bindParser(EventApplicator.class).to(EventApplicatorParser.class);

        final FeatureDefinitionParserBinder features = new FeatureDefinitionParserBinder(this.binder());
        features.bindFeatureParser(EventApplicator.class).to(EventApplicatorParser.class);

    }
}