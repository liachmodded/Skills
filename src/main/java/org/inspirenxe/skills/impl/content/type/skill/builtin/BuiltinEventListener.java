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
package org.inspirenxe.skills.impl.content.type.skill.builtin;

import static com.google.common.base.Preconditions.checkNotNull;

import com.almuradev.toolbox.inject.event.Witness;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.inspirenxe.skills.api.Result;
import org.inspirenxe.skills.api.Skill;
import org.inspirenxe.skills.api.SkillHolder;
import org.inspirenxe.skills.api.SkillManager;
import org.inspirenxe.skills.api.SkillType;
import org.inspirenxe.skills.api.event.ExperienceEvent;
import org.inspirenxe.skills.api.event.ExperienceResult;
import org.inspirenxe.skills.impl.content.type.skill.builtin.chain.BlockChain;
import org.inspirenxe.skills.impl.content.type.skill.builtin.chain.ItemChain;
import org.inspirenxe.skills.impl.content.type.skill.builtin.feedback.EventEffect;
import org.inspirenxe.skills.impl.content.type.skill.builtin.feedback.EventMessage;
import org.inspirenxe.skills.impl.content.type.skill.builtin.skill.CraftingRegistar;
import org.inspirenxe.skills.impl.content.type.skill.builtin.skill.FarmingRegistar;
import org.inspirenxe.skills.impl.content.type.skill.builtin.skill.MiningRegistar;
import org.inspirenxe.skills.impl.content.type.skill.builtin.skill.WoodcuttingRegistar;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.entity.projectile.Firework;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.item.inventory.CraftItemEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackComparators;
import org.spongepowered.api.item.recipe.crafting.CraftingRecipe;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.text.Text;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

// TODO Plan B. Move to scripting engine later.

@Singleton
public final class BuiltinEventListener implements Witness {

    private final SkillManager skillManager;
    private final Map<Class<? extends Event>, Map<SkillType, List<EventMessage>>> messageBuilders = new HashMap<>();
    private final Map<Class<? extends Event>, Map<SkillType, List<EventEffect>>> effectBuilders = new HashMap<>();
    private final Map<Class<? extends Event>, Map<SkillType, List<BlockChain>>> blockChains = new HashMap<>();
    private final Map<Class<? extends Event>, Map<SkillType, List<ItemChain>>> itemChains = new HashMap<>();

    private final Map<Chain<?>, Long> denyTimers = new HashMap<>();

    @Inject
    public BuiltinEventListener(final SkillManager skillManager) {
        this.skillManager = skillManager;
    }

    @Listener(order = Order.LAST)
    public void onGameStartedServer(final GameStartedServerEvent event) {
        this.configure();
    }

    @Listener
    public void onGameReload(final GameReloadEvent event) {
        this.configure();
    }

    @Listener(order = Order.PRE)
    public void onChangeBlockBreak(final ChangeBlockEvent.Break event, @Root final Player player) {
        this.handleChangeBlock(event, player, true);
    }

    @Listener(order = Order.PRE)
    public void onChangeBlockModify(final ChangeBlockEvent.Modify event, @Root final Player player) {
        this.handleChangeBlock(event, player, false);
    }

    @Listener(order = Order.PRE)
    public void onChangeBlockPlace(final ChangeBlockEvent.Place event, @Root final Player player) {
        this.handleChangeBlock(event, player, false);
    }

    @Listener(order = Order.PRE)
    public void onInteractItem(final InteractItemEvent event, @Root final Player player) {
        this.handleInteractItem(event, player);
    }

    @Listener(order = Order.PRE)
    public void onCraftItemCraft(final CraftItemEvent.Craft event, @First final Player player) {
        this.handleCraftItem(event, player);
    }

    @Listener(order = Order.PRE)
    public void onDropItemDestruct(final DropItemEvent.Destruct event, @Root final BlockSnapshot snapshot, @First final Player player) {
        this.handleDropItemEvent(event, player);
    }

    @Listener(order = Order.PRE)
    public void onChangeExperiencePost(final ExperienceEvent.Change.Post event, @First final Player player) {
        final Boolean sneaking = player.get(Keys.IS_SNEAKING).orElse(false);

        List<EventMessage> messages = null;
        List<EventEffect> effects = null;

        final Cause cause = event.getCause();
        final Event causeEvent = cause.first(Event.class).orElse(null);

        if (causeEvent != null) {
            messages = this.messageBuilders.entrySet()
                .stream()
                .filter(kv -> kv.getKey().isAssignableFrom(event.getClass()))
                .findAny()
                .map(Map.Entry::getValue)
                .orElse(new HashMap<>()).entrySet()
                .stream()
                .filter(kv -> kv.getKey() == event.getSkillType())
                .findAny()
                .map(Map.Entry::getValue)
                .orElse(new ArrayList<>());

            effects = this.effectBuilders.entrySet()
                .stream()
                .filter(kv -> kv.getKey().isAssignableFrom(event.getClass()))
                .findAny()
                .map(Map.Entry::getValue)
                .orElse(new HashMap<>()).entrySet()
                .stream()
                .filter(kv -> kv.getKey() == event.getSkillType())
                .findAny()
                .map(Map.Entry::getValue)
                .orElse(new ArrayList<>());
        }

        if (messages != null) {
            messages
                .stream()
                .filter(v -> v.xpGained != null)
                .forEach(v -> {
                    final Text message = v.xpGained.apply(player, event.getSkill(), event.getExperienceDifference());
                    if (message != null) {
                        player.sendMessage(v.chatType, message);
                    }
                });
        }

        if (event instanceof ExperienceEvent.Change.Post.Level) {

            if (event.getExperienceDifference() < 0) {
                return;
            }

            if (messages != null) {
                messages
                    .stream()
                    .filter(v -> v.levelGained != null)
                    .forEach(v -> {
                        final Text message = v.levelGained.apply(player, event.getSkill(), ((ExperienceEvent.Change.Post.Level) event).getLevel());
                        if (message != null) {
                            player.sendMessage(v.chatType, message);
                        }
                    });
            }

            if (!sneaking && effects != null) {
                try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                    frame.pushCause(event.getSkill());

                    effects
                        .stream()
                        .filter(v -> v.levelGained != null)
                        .forEach(effectBuilder -> effectBuilder.levelGained.apply(player, event.getSkill(),
                            ((ExperienceEvent.Change.Post.Level) event).getLevel())
                            .forEach(effect -> effect.play(player.getLocation(), player)));
                }
            }
        } else {
            if (!sneaking && effects != null) {
                // Xp effects
                effects
                    .stream()
                    .filter(v -> v.xpGained != null)
                    .forEach(effectBuilder -> effectBuilder.xpGained.apply(player, event.getSkill(), event.getExperienceDifference()).forEach(effect ->
                        effect.play(player.getLocation(), player)));
            }
        }
    }

    @Listener(order = Order.PRE)
    public void onDamageEntity(final DamageEntityEvent event, @Root final EntityDamageSource source, @First final Skill skill) {
        if (source.getSource() instanceof Firework) {
            event.setCancelled(true);
        }
    }

    private void configure() {
        this.blockChains.clear();
        this.effectBuilders.clear();
        this.itemChains.clear();
        this.messageBuilders.clear();
        this.denyTimers.clear();

        MiningRegistar.configure();
        CraftingRegistar.configure();
        FarmingRegistar.configure();
        WoodcuttingRegistar.configure();
    }

    private void handleChangeBlock(final ChangeBlockEvent event, final Player player, final boolean originalState) {
        if (player.gameMode().get() == GameModes.CREATIVE) {
            return;
        }

        final SkillHolder skillHolder =
            this.skillManager.getHolder(Sponge.getServer().getDefaultWorld().get().getUniqueId(), player.getUniqueId()).orElse(null);

        if (skillHolder == null) {
            return;
        }

        final Map<SkillType, List<BlockChain>> eventChains = this.blockChains.entrySet()
            .stream()
            .filter(kv -> kv.getKey().isAssignableFrom(event.getClass()))
            .findAny()
            .map(Map.Entry::getValue)
            .orElse(new HashMap<>());

        if (eventChains.isEmpty()) {
            return;
        }

        final Collection<Skill> skills = skillHolder.getSkills().values();

        final Set<Map.Entry<SkillType, List<BlockChain>>> skillChains = eventChains.entrySet()
            .stream()
            .filter(kv -> skills.stream().anyMatch(v -> v.getSkillType() == kv.getKey()))
            .collect(Collectors.toSet());

        final List<BuiltinResult> successResults = new ArrayList<>();

        for (final Transaction<BlockSnapshot> transaction : event.getTransactions()) {

            final BlockSnapshot snapshot = originalState ? transaction.getOriginal() : transaction.getFinal();

            final Collection<BuiltinResult> transactionResults = this.processBlockSnapshotFor(skills, skillChains, snapshot);

            final List<BuiltinResult> cancelledResults = transactionResults
                .stream()
                .filter(result -> result.getType() == Result.Type.CANCELLED)
                .collect(Collectors.toList());

            for (final BuiltinResult cancelledResult : cancelledResults) {
                transaction.setValid(false);

                final Chain<?> chain = cancelledResult.getChain().orElse(null);
                if (chain != null && chain.denyLevelRequired != null) {
                    final long stamp = System.currentTimeMillis();
                    final Long timer = this.denyTimers.computeIfAbsent(chain, (c) -> stamp - 5000);

                    final long diff = stamp - timer;

                    if (diff < 5000) {
                        continue;
                    }

                    this.denyTimers.put(chain, stamp);
                    chain.denyLevelRequired.accept(player, cancelledResult.getSkill(), chain.level);
                }
            }

            successResults.addAll(transactionResults
                .stream()
                .filter(result -> result.getType() == Result.Type.SUCCESS)
                .collect(Collectors.toList())
            );
        }

        final Map<Skill, Double> totalXpGained = new HashMap<>();
        final Map<Skill, BigDecimal> totalMoneyGained = new HashMap<>();

        for (final BuiltinResult successResult : successResults) {
            final Double xp = successResult.getXp().orElse(null);

            if (xp != null) {
                Double value = totalXpGained.get(successResult.getSkill());
                if (value != null) {
                    value = value + xp;
                } else {
                    value = xp;
                }

                totalXpGained.put(successResult.getSkill(), value);
            }

            final BigDecimal money = successResult.getMoney().orElse(null);
            if (money != null) {
                BigDecimal value = totalMoneyGained.get(successResult.getSkill());
                if (value != null) {
                    value = value.add(money);
                } else {
                    value = money;
                }

                totalMoneyGained.put(successResult.getSkill(), value);
            }
        }

        for (final Map.Entry<Skill, Double> entry : totalXpGained.entrySet()) {
            final Skill skill = entry.getKey();
            final Double value = entry.getValue();

            try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                frame.pushCause(skill);
                frame.pushCause(event);

                final ExperienceResult result = skill.addExperience(value);
                if (result.getType() == Result.Type.CANCELLED) {
                    totalMoneyGained.remove(skill);
                }
            }
        }

        final EconomyService economyService = Sponge.getServiceManager().provide(EconomyService.class).orElse(null);

        if (economyService == null) {
            return;
        }

        final UniqueAccount account = economyService.getOrCreateAccount(player.getUniqueId()).orElse(null);
        if (account == null) {
            return;
        }

        for (final Map.Entry<Skill, BigDecimal> entry : totalMoneyGained.entrySet()) {
            final Skill skill = entry.getKey();
            final BigDecimal value = entry.getValue();

            try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                frame.pushCause(skill);
                frame.pushCause(event);

                account.deposit(economyService.getDefaultCurrency(), value, frame.getCurrentCause());
            }
        }
    }

    private void handleInteractItem(final InteractItemEvent event, final Player player) {
        if (player.gameMode().get() == GameModes.CREATIVE) {
            return;
        }

        final SkillHolder skillHolder =
            this.skillManager.getHolder(Sponge.getServer().getDefaultWorld().get().getUniqueId(), player.getUniqueId()).orElse(null);

        if (skillHolder == null) {
            return;
        }

        final Map<SkillType, List<ItemChain>> eventChains = this.itemChains.entrySet()
            .stream()
            .filter(kv -> kv.getKey().isAssignableFrom(event.getClass()))
            .findAny()
            .map(Map.Entry::getValue)
            .orElse(new HashMap<>());

        if (eventChains.isEmpty()) {
            return;
        }

        final Collection<Skill> skills = skillHolder.getSkills().values();

        final Set<Map.Entry<SkillType, List<ItemChain>>> skillChains = eventChains.entrySet()
            .stream()
            .filter(kv -> skills.stream().anyMatch(v -> v.getSkillType() == kv.getKey()))
            .collect(Collectors.toSet());

        if (skillChains.isEmpty()) {
            return;
        }

        final ItemStack stack = event.getItemStack().createStack();

        final Collection<BuiltinResult> results = this.handleItemStack(skills, skillChains, stack);

        final List<BuiltinResult> cancelledResults = results
            .stream()
            .filter(result -> result.getType() == Result.Type.CANCELLED)
            .collect(Collectors.toList());

        for (final BuiltinResult cancelledResult : cancelledResults) {
            event.setCancelled(true);

            final Chain<?> chain = cancelledResult.getChain().orElse(null);
            if (chain != null && chain.denyLevelRequired != null) {
                final long stamp = System.currentTimeMillis();
                final Long timer = this.denyTimers.computeIfAbsent(chain, (c) -> stamp - 5000);

                final long diff = stamp - timer;

                if (diff < 5000) {
                    continue;
                }

                this.denyTimers.put(chain, stamp);
                chain.denyLevelRequired.accept(player, cancelledResult.getSkill(), chain.level);
            }
        }

        if (event.isCancelled()) {
            return;
        }

        final List<BuiltinResult> successResults = results
            .stream()
            .filter(result -> result.getType() == Result.Type.SUCCESS)
            .collect(Collectors.toList());

        final Map<Skill, Double> totalXpGained = new HashMap<>();
        final Map<Skill, BigDecimal> totalMoneyGained = new HashMap<>();

        for (final BuiltinResult successResult : successResults) {
            final Double xp = successResult.getXp().orElse(null);

            if (xp != null) {
                Double value = totalXpGained.get(successResult.getSkill());
                if (value != null) {
                    value = value + xp;
                } else {
                    value = xp;
                }

                totalXpGained.put(successResult.getSkill(), value);
            }

            final BigDecimal money = successResult.getMoney().orElse(null);
            if (money != null) {
                BigDecimal value = totalMoneyGained.get(successResult.getSkill());
                if (value != null) {
                    value = value.add(money);
                } else {
                    value = money;
                }

                totalMoneyGained.put(successResult.getSkill(), value);
            }
        }

        for (final Map.Entry<Skill, Double> entry : totalXpGained.entrySet()) {
            final Skill skill = entry.getKey();
            final Double value = entry.getValue();

            try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                frame.pushCause(skill);
                frame.pushCause(event);

                final ExperienceResult result = skill.addExperience(value);
                if (result.getType() == Result.Type.CANCELLED) {
                    totalMoneyGained.remove(skill);
                }
            }
        }

        final EconomyService economyService = Sponge.getServiceManager().provide(EconomyService.class).orElse(null);

        if (economyService == null) {
            return;
        }

        final UniqueAccount account = economyService.getOrCreateAccount(player.getUniqueId()).orElse(null);
        if (account == null) {
            return;
        }

        for (final Map.Entry<Skill, BigDecimal> entry : totalMoneyGained.entrySet()) {
            final Skill skill = entry.getKey();
            final BigDecimal value = entry.getValue();

            try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                frame.pushCause(skill);
                frame.pushCause(event);

                account.deposit(economyService.getDefaultCurrency(), value, frame.getCurrentCause());
            }
        }
    }

    private void handleCraftItem(final CraftItemEvent.Craft event, final Player player) {
        if (player.gameMode().get() == GameModes.CREATIVE) {
            return;
        }

        final SkillHolder skillHolder =
            this.skillManager.getHolder(Sponge.getServer().getDefaultWorld().get().getUniqueId(), player.getUniqueId()).orElse(null);

        if (skillHolder == null) {
            return;
        }

        final Map<SkillType, List<ItemChain>> eventChains = this.itemChains.entrySet()
            .stream()
            .filter(kv -> kv.getKey().isAssignableFrom(event.getClass()))
            .findAny()
            .map(Map.Entry::getValue)
            .orElse(new HashMap<>());

        if (eventChains.isEmpty()) {
            return;
        }

        final Collection<Skill> skills = skillHolder.getSkills().values();

        final Set<Map.Entry<SkillType, List<ItemChain>>> skillChains = eventChains.entrySet()
            .stream()
            .filter(kv -> skills.stream().anyMatch(v -> v.getSkillType() == kv.getKey()))
            .collect(Collectors.toSet());

        if (skillChains.isEmpty()) {
            return;
        }

        final List<BuiltinResult> results = new ArrayList<>();

        final List<ItemStack> stacks = new ArrayList<>();

        final ItemStack crafted = event.getCrafted().createStack();

        final CraftingRecipe recipe = event.getRecipe().orElse(null);
        if (recipe != null) {
            final ItemStack defaultResult = recipe.getExemplaryResult().createStack();

            final int craftedTimes = crafted.getQuantity() / defaultResult.getQuantity();

            for (int i = 0; i < craftedTimes; i++) {
                final ItemStack copyStack = crafted.copy();
                copyStack.setQuantity(defaultResult.getQuantity());
                stacks.add(copyStack);
            }
        } else {
            stacks.add(crafted);
        }

        for (final ItemStack stack : stacks) {
            results.addAll(this.handleItemStack(skills, skillChains, stack));
        }

        final List<BuiltinResult> cancelledResults = results
            .stream()
            .filter(result -> result.getType() == Result.Type.CANCELLED)
            .collect(Collectors.toList());

        for (final BuiltinResult cancelledResult : cancelledResults) {
            event.setCancelled(true);

            final Chain<?> chain = cancelledResult.getChain().orElse(null);

            if (chain != null && chain.denyLevelRequired != null) {
                chain.denyLevelRequired.accept(player, cancelledResult.getSkill(), chain.level);
            }
        }

        if (event.isCancelled()) {
            return;
        }

        final List<BuiltinResult> successResults = results
            .stream()
            .filter(result -> result.getType() == Result.Type.SUCCESS)
            .collect(Collectors.toList());

        final Map<Skill, Double> totalXpGained = new HashMap<>();
        final Map<Skill, BigDecimal> totalMoneyGained = new HashMap<>();

        for (final BuiltinResult successResult : successResults) {
            final Double xp = successResult.getXp().orElse(null);

            if (xp != null) {
                Double value = totalXpGained.get(successResult.getSkill());
                if (value != null) {
                    value = value + xp;
                } else {
                    value = xp;
                }

                totalXpGained.put(successResult.getSkill(), value);
            }

            final BigDecimal money = successResult.getMoney().orElse(null);
            if (money != null) {
                BigDecimal value = totalMoneyGained.get(successResult.getSkill());
                if (value != null) {
                    value = value.add(money);
                } else {
                    value = money;
                }

                totalMoneyGained.put(successResult.getSkill(), value);
            }
        }

        for (final Map.Entry<Skill, Double> entry : totalXpGained.entrySet()) {
            final Skill skill = entry.getKey();
            final Double value = entry.getValue();

            try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                frame.pushCause(skill);
                frame.pushCause(event);

                final ExperienceResult result = skill.addExperience(value);
                if (result.getType() == Result.Type.CANCELLED) {
                    totalMoneyGained.remove(skill);
                }
            }
        }

        final EconomyService economyService = Sponge.getServiceManager().provide(EconomyService.class).orElse(null);

        if (economyService == null) {
            return;
        }

        final UniqueAccount account = economyService.getOrCreateAccount(player.getUniqueId()).orElse(null);
        if (account == null) {
            return;
        }

        for (final Map.Entry<Skill, BigDecimal> entry : totalMoneyGained.entrySet()) {
            final Skill skill = entry.getKey();
            final BigDecimal value = entry.getValue();

            try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                frame.pushCause(skill);
                frame.pushCause(event);

                account.deposit(economyService.getDefaultCurrency(), value, frame.getCurrentCause());
            }
        }
    }

    private void handleDropItemEvent(final DropItemEvent.Destruct event, final Player player) {
        if (player.gameMode().get() == GameModes.CREATIVE) {
            return;
        }

        final List<Item> items = event.getEntities()
            .stream()
            .filter(entity -> entity instanceof Item)
            .map(entity -> (Item) entity)
            .collect(Collectors.toList());

        if (items.isEmpty()) {
            return;
        }

        final SkillHolder skillHolder =
            this.skillManager.getHolder(Sponge.getServer().getDefaultWorld().get().getUniqueId(), player.getUniqueId()).orElse(null);

        if (skillHolder == null) {
            return;
        }

        final Map<SkillType, List<ItemChain>> eventChains = this.itemChains.entrySet()
            .stream()
            .filter(kv -> kv.getKey().isAssignableFrom(event.getClass()))
            .findAny()
            .map(Map.Entry::getValue)
            .orElse(new HashMap<>());

        if (eventChains.isEmpty()) {
            return;
        }

        final Collection<Skill> skills = skillHolder.getSkills().values();

        final Set<Map.Entry<SkillType, List<ItemChain>>> skillChains = eventChains.entrySet()
            .stream()
            .filter(kv -> skills.stream().anyMatch(v -> v.getSkillType() == kv.getKey()))
            .collect(Collectors.toSet());

        if (skillChains.isEmpty()) {
            return;
        }

        final List<BuiltinResult> successResults = new ArrayList<>();

        for (final Item item : items) {
            final Collection<BuiltinResult> itemResults = this.handleItemStack(skills, skillChains, item.item().get().createStack());
            final List<BuiltinResult> cancelledResults = itemResults
                .stream()
                .filter(result -> result.getType() == Result.Type.CANCELLED)
                .collect(Collectors.toList());

            for (final BuiltinResult cancelledResult : cancelledResults) {
                final Chain<?> chain = cancelledResult.getChain().orElse(null);
                if (chain != null && chain.denyLevelRequired != null) {
                    chain.denyLevelRequired.accept(player, cancelledResult.getSkill(), chain.level);
                }

                event.filterEntities(entity -> entity == item);
            }

            successResults.addAll(itemResults
                .stream()
                .filter(result -> result.getType() == Result.Type.SUCCESS)
                .collect(Collectors.toList())
            );
        }

        final Map<Skill, Double> totalXpGained = new HashMap<>();
        final Map<Skill, BigDecimal> totalMoneyGained = new HashMap<>();

        for (final BuiltinResult successResult : successResults) {
            final Double xp = successResult.getXp().orElse(null);

            if (xp != null) {
                Double value = totalXpGained.get(successResult.getSkill());
                if (value != null) {
                    value = value + xp;
                } else {
                    value = xp;
                }

                totalXpGained.put(successResult.getSkill(), value);
            }

            final BigDecimal money = successResult.getMoney().orElse(null);
            if (money != null) {
                BigDecimal value = totalMoneyGained.get(successResult.getSkill());
                if (value != null) {
                    value = value.add(money);
                } else {
                    value = money;
                }

                totalMoneyGained.put(successResult.getSkill(), value);
            }
        }

        for (final Map.Entry<Skill, Double> entry : totalXpGained.entrySet()) {
            final Skill skill = entry.getKey();
            final Double value = entry.getValue();

            try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                frame.pushCause(skill);
                frame.pushCause(event);

                final ExperienceResult result = skill.addExperience(value);
                if (result.getType() == Result.Type.CANCELLED) {
                    totalMoneyGained.remove(skill);
                }
            }
        }

        final EconomyService economyService = Sponge.getServiceManager().provide(EconomyService.class).orElse(null);

        if (economyService == null) {
            return;
        }

        final UniqueAccount account = economyService.getOrCreateAccount(player.getUniqueId()).orElse(null);
        if (account == null) {
            return;
        }

        for (final Map.Entry<Skill, BigDecimal> entry : totalMoneyGained.entrySet()) {
            final Skill skill = entry.getKey();
            final BigDecimal value = entry.getValue();

            try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                frame.pushCause(skill);
                frame.pushCause(event);

                account.deposit(economyService.getDefaultCurrency(), value, frame.getCurrentCause());
            }
        }
    }

    private Collection<BuiltinResult> processBlockSnapshotFor(final Collection<Skill> skills, final Set<Map.Entry<SkillType, List<BlockChain>>>
        skillChains, final BlockSnapshot snapshot) {

        final List<BuiltinResult> results = new ArrayList<>();

        for (final Skill skill : skills) {

            final BuiltinResult.Builder builder = BuiltinResult.builder().skill(skill);

            final List<BlockChain> chains = skillChains
                .stream()
                .filter(kv -> kv.getKey() == skill.getSkillType())
                .findAny()
                .map(Map.Entry::getValue)
                .orElse(new ArrayList<>());

            final BlockChain chain = chains
                .stream()
                .filter(v -> {
                    if (v.matchOnlyType) {
                        return v.inverseQuery ? v.toQuery.stream().noneMatch(s -> s.getType() == snapshot.getState().getType()) :
                            v.toQuery.stream().anyMatch(s -> s.getType() == snapshot.getState().getType());
                    } else {
                        return v.inverseQuery != v.toQuery.contains(snapshot.getState());
                    }
                })
                .findAny()
                .orElse(null);

            if (chain == null) {
                continue;
            }

            builder.type(Result.Type.SUCCESS);

            if (chain != null) {

                builder.chain(chain);

                if (chain.level != null && chain.level > skill.getCurrentLevel()) {
                    builder.type(Result.Type.CANCELLED);
                } else {
                    if (chain.economy != null) {
                        skill.getSkillType().getEconomyFunction()
                            .ifPresent(func -> builder.money(func.getMoneyFor(skill.getCurrentLevel(), chain.economy)));
                    }

                    if (chain.xp != null) {
                        builder.xp(chain.xp);
                    }
                }
            }

            results.add(builder.build());
        }

        return results;
    }

    private Collection<BuiltinResult> handleItemStack(final Collection<Skill> skills, final Set<Map.Entry<SkillType, List<ItemChain>>>
        skillChains, final ItemStack stack) {

        final List<BuiltinResult> results = new ArrayList<>();

        for (final Skill skill : skills) {

            final BuiltinResult.Builder builder = BuiltinResult.builder().skill(skill);

            final List<ItemChain> chains = skillChains
                .stream()
                .filter(kv -> kv.getKey() == skill.getSkillType())
                .findAny()
                .map(Map.Entry::getValue)
                .orElse(new ArrayList<>());

            final ItemChain chain = chains
                .stream()
                .filter(v -> {
                    if (v.matchOnlyType) {
                        return v.inverseQuery ?
                            v.toQuery.stream().noneMatch(s -> ItemStackComparators.TYPE.compare(s, stack) == 0 && ItemStackComparators.ITEM_DATA_IGNORE_DAMAGE.compare(s, stack) == 0) :
                            v.toQuery.stream().anyMatch(s -> ItemStackComparators.TYPE.compare(s, stack) == 0 && ItemStackComparators.ITEM_DATA_IGNORE_DAMAGE.compare(s, stack) == 0);
                    } else {
                        return v.inverseQuery ? v.toQuery.stream().noneMatch(s -> ItemStackComparators.ALL.compare(s, stack) == 0) :
                            v.toQuery.stream().anyMatch(s -> ItemStackComparators.ALL.compare(s, stack) == 0);
                    }
                })
                .findAny()
                .orElse(null);

            builder.type(Result.Type.SUCCESS);

            if (chain != null) {

                builder.chain(chain);

                if (chain.level != null && chain.level > skill.getCurrentLevel()) {
                    builder.type(Result.Type.CANCELLED);
                } else {
                    if (chain.economy != null) {
                        skill.getSkillType().getEconomyFunction()
                            .ifPresent(func -> builder.money(func.getMoneyFor(skill.getCurrentLevel(), chain.economy)));
                    }

                    if (chain.xp != null) {
                        builder.xp(chain.xp);
                    }
                }
            }

            results.add(builder.build());
        }

        return results;
    }

    public BuiltinEventListener addEffectChain(final Class<? extends Event> clazz, final SkillType type, final EventEffect builder) {
        checkNotNull(type);
        checkNotNull(builder);

        this.effectBuilders.computeIfAbsent(clazz, v -> new HashMap<>()).computeIfAbsent(type, v -> new ArrayList<>()).add(builder);
        return this;
    }

    public BuiltinEventListener addMessageChain(final Class<? extends Event> clazz, final SkillType type, final EventMessage builder) {
        checkNotNull(type);
        checkNotNull(builder);

        this.messageBuilders.computeIfAbsent(clazz, v -> new HashMap<>()).computeIfAbsent(type, v -> new ArrayList<>()).add(builder);
        return this;
    }

    public BuiltinEventListener addBlockChain(final Class<? extends Event> clazz, final SkillType type, final BlockChain builder) {
        checkNotNull(type);
        checkNotNull(builder);

        this.blockChains.computeIfAbsent(clazz, v -> new HashMap<>()).computeIfAbsent(type, v -> new ArrayList<>()).add(builder);
        return this;
    }

    public BuiltinEventListener addItemChain(final Class<? extends Event> clazz, final SkillType type, final ItemChain builder) {
        checkNotNull(type);
        checkNotNull(builder);

        this.itemChains.computeIfAbsent(clazz, v -> new HashMap<>()).computeIfAbsent(type, v -> new ArrayList<>()).add(builder);
        return this;
    }
}
