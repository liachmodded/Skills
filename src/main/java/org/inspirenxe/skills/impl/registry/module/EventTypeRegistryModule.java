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
package org.inspirenxe.skills.impl.registry.module;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Singleton;
import org.inspirenxe.skills.impl.content.type.skill.component.event.EventType;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.action.InteractEvent;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Singleton
public final class EventTypeRegistryModule implements AdditionalCatalogRegistryModule<EventType> {

  public static final EventTypeRegistryModule instance = new EventTypeRegistryModule();

  private final Map<String, EventType> map = new HashMap<>();

  public EventTypeRegistryModule() {
    this.registerDefaults();
  }

  @Override
  public void registerDefaults() {
    final EventType eventType = EventType.of("event", Event.class);
    this.registerAdditionalCatalog(eventType);

    final EventType changeBlockType = eventType.child("change-block", ChangeBlockEvent.class);
    this.registerAdditionalCatalog(changeBlockType);
    this.registerAdditionalCatalog(changeBlockType.child("break", ChangeBlockEvent.Break.class));
    this.registerAdditionalCatalog(changeBlockType.child("place", ChangeBlockEvent.Place.class));

    final EventType interactType = eventType.child("interact", InteractEvent.class);
    this.registerAdditionalCatalog(interactType);
    this.registerAdditionalCatalog(interactType.child("item", InteractItemEvent.class));
  }

  @Override
  public void registerAdditionalCatalog(EventType extraCatalog) {
    checkNotNull(extraCatalog);
    this.map.put(extraCatalog.getId(), extraCatalog);
  }

  @Override
  public Optional<EventType> getById(String id) {
    return Optional.ofNullable(this.map.get(id));
  }

  @Override
  public Collection<EventType> getAll() {
    return Collections.unmodifiableCollection(this.map.values());
  }
}
