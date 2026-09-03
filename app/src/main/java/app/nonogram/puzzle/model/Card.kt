package app.nonogram.puzzle.model

import androidx.compose.ui.graphics.Color

enum class Rarity(val label: String, val color: Color, val glow: Color) {
    COMMON("Common", Color(0xFF7D8896), Color(0x337D8896)),
    RARE("Rare", Color(0xFF2E7DD1), Color(0x332E7DD1)),
    EPIC("Epic", Color(0xFF8B5CF6), Color(0x338B5CF6)),
    LEGENDARY("Legendary", Color(0xFFF2A93B), Color(0x55F2A93B)),
}

/** One collectible card. [number] 1 is the crown (best score), 39 the entry card. */
class Card(
    val number: Int,
    val name: String,
    val emoji: String,
    val rarity: Rarity,
)

/**
 * The 39-card deck plus the deterministic score -> card mapping.
 * A given score always yields the same card on every device — no randomness.
 */
object CardDeck {

    // Ordered #1 (rarest) .. #39. Theme: creatures rising from humble critters to the mythical.
    val cards: List<Card> = buildList {
        // Legendary 1-3
        add(Card(1, "Ancient Dragon", "🐉", Rarity.LEGENDARY))
        add(Card(2, "Unicorn", "🦄", Rarity.LEGENDARY))
        add(Card(3, "Phoenix", "🦅", Rarity.LEGENDARY))
        // Epic 4-12
        add(Card(4, "Lion", "🦁", Rarity.EPIC))
        add(Card(5, "Tiger", "🐅", Rarity.EPIC))
        add(Card(6, "Wolf", "🐺", Rarity.EPIC))
        add(Card(7, "Bear", "🐻", Rarity.EPIC))
        add(Card(8, "Shark", "🦈", Rarity.EPIC))
        add(Card(9, "Elephant", "🐘", Rarity.EPIC))
        add(Card(10, "Owl", "🦉", Rarity.EPIC))
        add(Card(11, "Stag", "🦌", Rarity.EPIC))
        add(Card(12, "Octopus", "🐙", Rarity.EPIC))
        // Rare 13-24
        add(Card(13, "Fox", "🦊", Rarity.RARE))
        add(Card(14, "Dolphin", "🐬", Rarity.RARE))
        add(Card(15, "Parrot", "🦜", Rarity.RARE))
        add(Card(16, "Swan", "🦢", Rarity.RARE))
        add(Card(17, "Turtle", "🐢", Rarity.RARE))
        add(Card(18, "Dino", "🦕", Rarity.RARE))
        add(Card(19, "Flamingo", "🦩", Rarity.RARE))
        add(Card(20, "Koala", "🐨", Rarity.RARE))
        add(Card(21, "Panda", "🐼", Rarity.RARE))
        add(Card(22, "Peacock", "🦚", Rarity.RARE))
        add(Card(23, "Hedgehog", "🦔", Rarity.RARE))
        add(Card(24, "Bat", "🦇", Rarity.RARE))
        // Common 25-39
        add(Card(25, "Cat", "🐱", Rarity.COMMON))
        add(Card(26, "Dog", "🐶", Rarity.COMMON))
        add(Card(27, "Rabbit", "🐰", Rarity.COMMON))
        add(Card(28, "Mouse", "🐭", Rarity.COMMON))
        add(Card(29, "Frog", "🐸", Rarity.COMMON))
        add(Card(30, "Fish", "🐟", Rarity.COMMON))
        add(Card(31, "Bee", "🐝", Rarity.COMMON))
        add(Card(32, "Ladybug", "🐞", Rarity.COMMON))
        add(Card(33, "Snail", "🐌", Rarity.COMMON))
        add(Card(34, "Chick", "🐤", Rarity.COMMON))
        add(Card(35, "Duck", "🦆", Rarity.COMMON))
        add(Card(36, "Pig", "🐷", Rarity.COMMON))
        add(Card(37, "Cow", "🐮", Rarity.COMMON))
        add(Card(38, "Hamster", "🐹", Rarity.COMMON))
        add(Card(39, "Butterfly", "🦋", Rarity.COMMON))
    }

    val size: Int get() = cards.size

    fun card(number: Int): Card = cards[number - 1]

    /**
     * Maps a score to a card number (1..39). Deterministic and total.
     *  - >= 400 : card 1
     *  - >= 380 : card 2
     *  - >= 360 : card 3
     *  - below  : 10-point bands, card 4 covers 350-359 down to card 39 for 0-9.
     */
    fun cardNumberForScore(score: Int): Int = when {
        score >= 400 -> 1
        score >= 380 -> 2
        score >= 360 -> 3
        else -> {
            val s = score.coerceIn(0, 359)
            (4 + (359 - s) / 10).coerceIn(4, 39)
        }
    }

    fun cardForScore(score: Int): Card = card(cardNumberForScore(score))
}
