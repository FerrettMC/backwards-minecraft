package com.ferrett.backwardsmc;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;

import java.util.List;

public class Book {

    private static final String NBT_KEY = "BackwardsMC_GotGuideBook";

    public static void giveIfNeeded(Player player) {
        var persistentData = player.getPersistentData();

        boolean alreadyGiven = persistentData.getBoolean(NBT_KEY).orElse(false);
        if (alreadyGiven) return;

        persistentData.putBoolean(NBT_KEY, true);

        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);

        List<Filterable<Component>> pages = List.of(
                // Title page
                Filterable.passThrough(Component.literal("")
                        .append(Component.literal("Backwards Minecraft\n\n")
                                .withStyle(style -> style.withBold(true).withColor(0xFFD700)))
                        .append(Component.literal("Everything you knew is wrong.\nHere's what's flipped:")
                                .withStyle(style -> style.withColor(0x000000)))
                ),

                // 1
                Filterable.passThrough(Component.literal("")
                        .append(Component.literal("1. ").withStyle(style -> style.withColor(0x00FFFF).withBold(true)))
                        .append(Component.literal("You wake up in the End, not the Overworld.\n\nBuild a 3x4 dirt portal to get back home.")
                                .withStyle(style -> style.withColor(0x000000)))
                ),

                // 2
                Filterable.passThrough(Component.literal("")
                        .append(Component.literal("2. ").withStyle(style -> style.withColor(0x00FFFF).withBold(true)))
                        .append(Component.literal("Death is a gift, not a punishment.\n\nDie and you'll respawn at full health with a bonus item.")
                                .withStyle(style -> style.withColor(0x000000)))
                ),

                // 3
                Filterable.passThrough(Component.literal("")
                        .append(Component.literal("3. ").withStyle(style -> style.withColor(0x00FFFF).withBold(true)))
                        .append(Component.literal("Villagers attack on sight.\n\nZombies are friendly and trade with you instead — find one for a powerful loop trade.")
                                .withStyle(style -> style.withColor(0x000000)))
                ),

                // 4
                Filterable.passThrough(Component.literal("")
                        .append(Component.literal("4. ").withStyle(style -> style.withColor(0x00FFFF).withBold(true)))
                        .append(Component.literal("Skeletons swing melee, spiders shoot arrows.\n\nThe ranged and close‑quarters roles are swapped.")
                                .withStyle(style -> style.withColor(0x000000)))
                ),

                // 5
                Filterable.passThrough(Component.literal("")
                        .append(Component.literal("5. ").withStyle(style -> style.withColor(0x00FFFF).withBold(true)))
                        .append(Component.literal("Day and night are swapped.\n\nMobs spawn in daylight and burn at night.\nSleep during the day.")
                                .withStyle(style -> style.withColor(0x000000)))
                ),

                // 6
                Filterable.passThrough(Component.literal("")
                        .append(Component.literal("6. ").withStyle(style -> style.withColor(0x00FFFF).withBold(true)))
                        .append(Component.literal("Wood tools outperform netherite.\n\nTool speed and damage scale backwards by tier.")
                                .withStyle(style -> style.withColor(0x000000)))
                ),

                // 7
                Filterable.passThrough(Component.literal("")
                        .append(Component.literal("7. ").withStyle(style -> style.withColor(0x00FFFF).withBold(true)))
                        .append(Component.literal("Eating food drains your hunger.\n\nEating ores (iron, gold, coal, etc.) fills it instead.")
                                .withStyle(style -> style.withColor(0x000000)))
                ),

                // 8
                Filterable.passThrough(Component.literal("")
                        .append(Component.literal("8. ").withStyle(style -> style.withColor(0x00FFFF).withBold(true)))
                        .append(Component.literal("Standing still slowly drains hunger.\n\nSprinting restores it — keep moving to stay fed.")
                                .withStyle(style -> style.withColor(0x000000)))
                ),

                // 9
                Filterable.passThrough(Component.literal("")
                        .append(Component.literal("9. ").withStyle(style -> style.withColor(0x00FFFF).withBold(true)))
                        .append(Component.literal("Logs drop stone, stone drops logs.\n\nWood gathering and stone mining are reversed.")
                                .withStyle(style -> style.withColor(0x000000)))
                ),

                // 10
                Filterable.passThrough(Component.literal("")
                        .append(Component.literal("10. ").withStyle(style -> style.withColor(0x00FFFF).withBold(true)))
                        .append(Component.literal("Gravity runs in reverse for loose materials.\n\nSand, gravel, and anvils fall upward.")
                                .withStyle(style -> style.withColor(0x000000)))
                ),

                // Final page
                Filterable.passThrough(Component.literal("")
                        .append(Component.literal("Good luck out there.\n\n")
                                .withStyle(style -> style.withColor(0xFFD700).withBold(true)))
                        .append(Component.literal("Nothing here works the way you remember.")
                                .withStyle(style -> style.withColor(0x000000)))
                )
        );


        WrittenBookContent content = new WrittenBookContent(
                Filterable.passThrough("Backwards Minecraft"),
                "BackwardsMC",
                0,
                pages,
                true
        );

        book.set(DataComponents.WRITTEN_BOOK_CONTENT, content);

        player.getInventory().add(book);
    }
}