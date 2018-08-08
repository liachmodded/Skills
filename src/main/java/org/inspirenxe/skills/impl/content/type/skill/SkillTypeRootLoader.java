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
package org.inspirenxe.skills.impl.content.type.skill;

import com.almuradev.droplet.content.loader.RootContentLoaderImpl;
import com.almuradev.droplet.registry.Registry;
import com.almuradev.toolbox.inject.event.Witness;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.inspirenxe.skills.api.SkillType;
import org.inspirenxe.skills.impl.SkillTypeImpl;
import org.inspirenxe.skills.impl.SkillsImpl;
import org.inspirenxe.skills.impl.content.type.skill.component.event.EventScript;
import org.inspirenxe.skills.impl.content.type.skill.component.event.EventType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.game.state.GameInitializationEvent;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Singleton
public final class SkillTypeRootLoader extends RootContentLoaderImpl<ContentSkillType.Child, ContentSkillTypeBuilder> implements Witness {

  private final Registry<SkillType> registry;

  @Inject
  public SkillTypeRootLoader(final Registry<SkillType> registry) {
    this.registry = registry;
  }

  @Listener(order = Order.FIRST)
  public void onGameStartedServer(GameInitializationEvent event) {
    this.foundContent().entries().forEach(entry -> this.registry.put(entry.key(), entry.result(SkillTypeImpl.class)));
    this.registerListeners();
  }

  private void registerListeners() {
    Multimap<Class<? extends Event>, SkillType> listeners = HashMultimap.create();
    Set<EventType<?>> withListeners = new HashSet<>();
    for (SkillType skillType: this.registry.all()) {
      for (Map.Entry<EventType<?>, EventScript> entry: skillType.getEventScripts().entrySet()) {
        listeners.put(entry.getKey().getEventClass(), skillType);
        if (entry.getValue().getBranches().size() != 0) {
          withListeners.add(entry.getKey());
        }
      }
    }

    for (SkillType skillType: this.registry.all()) {
      for (Map.Entry<EventType<?>, EventScript> entry: skillType.getEventScripts().entrySet()) {
        listeners.put(this.getOldestParent(withListeners, entry.getKey()), skillType);
      }
    }

    for (Class<? extends Event> eventType: listeners.keySet()) {
      DelegatingSkillEventListener listener = new DelegatingSkillEventListener(listeners.get(eventType));
      Sponge.getEventManager().registerListener(SkillsImpl.INSTANCE, eventType, listener);
    }
  }

  private Class<? extends Event> getOldestParent(Set<EventType<?>> withListeners, EventType<?> eventType) {
    EventType<?> highestWithListeners = eventType;
      while (eventType.getParent().isPresent()) {
        EventType<?> parent = eventType.getParent().get();
        if (withListeners.contains(parent)) {
          highestWithListeners = parent;
        }
        eventType = parent;
      }
      return highestWithListeners.getEventClass();
    }
}
