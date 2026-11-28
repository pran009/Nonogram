package app.nonogram.puzzle.data

import androidx.annotation.DrawableRes
import app.nonogram.puzzle.R

/**
 * The surprise card shown after solving a puzzle in the Animals, Space & Sky and Masterworks
 * packs: a photo, a short "About", a fun fact and a secret (unlocked by a no-mistake solve or
 * by watching 2 ads).
 */
data class Fact(
    val about: String,
    val funFact: String,
    val secret: String,
    @DrawableRes val image: Int = 0,
    /** Photo source, shown under the photo. All photos are public domain or CC0. */
    val credit: String = "",
)

object Facts {
    fun of(puzzleId: String): Fact? = all[puzzleId]

    private val all: Map<String, Fact> = mapOf(
        // ---- Animals ------------------------------------------------------------------------
        "a01" to Fact(
            about = "Elephants are the largest land animals on Earth. There are three species: the African savanna elephant, the African forest elephant and the Asian elephant. They live in close family groups led by the oldest female, the matriarch.",
            funFact = "An elephant's trunk has tens of thousands of muscles. It is strong enough to lift a log, yet gentle enough to pick up a single peanut.",
            secret = "Elephants can \"hear\" with their feet. They pick up the low rumbles of other elephants as vibrations in the ground, from kilometres away.",
            image = R.drawable.fact_a01,
            credit = "Hobbyfotowiki (CC0, Wikimedia Commons)",
        ),
        "a02" to Fact(
            about = "Frogs are amphibians. Most start life as tadpoles in water, breathing with gills, then grow legs and lungs so they can live on land. There are more than 7,000 kinds, on every continent except Antarctica.",
            funFact = "Many frogs use their eyes to help them swallow: they blink, pulling their eyeballs down into the head to push food down the throat.",
            secret = "The wood frog of North America can survive being frozen solid in winter. Its heart stops beating, and in spring it thaws out and hops away.",
            image = R.drawable.fact_a02,
            credit = "Careyjamesbalboa (public domain, Wikimedia Commons)",
        ),
        "a03" to Fact(
            about = "Penguins are seabirds that cannot fly in the air, but \"fly\" underwater with their stiff flippers. Almost all of the 18 or so species live in the Southern Hemisphere, from Antarctica to the Galápagos Islands.",
            funFact = "Emperor penguins can dive deeper than 500 metres and hold their breath for more than 20 minutes.",
            secret = "Penguins can drink seawater. A special gland above their eyes filters out the salt, which they then sneeze or shake away.",
            image = R.drawable.fact_a03,
            credit = "ravas51 (public domain, Wikimedia Commons)",
        ),
        "a04" to Fact(
            about = "Rabbits are small plant-eating mammals with long ears and powerful back legs. Many wild rabbits live in groups and dig networks of burrows called warrens.",
            funFact = "A rabbit's teeth never stop growing. Chewing grass and hay all day keeps them worn down to the right length.",
            secret = "When a rabbit is really happy it may do a \"binky\": a sudden leap into the air with a twist of the body and a flick of the feet.",
            image = R.drawable.fact_a04,
            credit = "U.S. Fish and Wildlife Service - Midwest Region (public domain, Wikimedia Commons)",
        ),
        "a05" to Fact(
            about = "Snails are mollusks that carry a spiral shell and glide along on a single muscular foot. Land snails leave a trail of slime that helps them move and protects their soft bodies.",
            funFact = "A garden snail's mouth holds thousands of microscopic teeth, lined up on a ribbon-like tongue called a radula.",
            secret = "In dry weather a snail can seal its shell with a layer of dried mucus and sleep for months until the rain returns.",
            image = R.drawable.fact_a05,
            credit = "Unknown author (public domain, Wikimedia Commons)",
        ),
        "a06" to Fact(
            about = "Whales are mammals that spend their whole lives in the ocean, coming up to breathe air through blowholes on top of their heads. The blue whale is the largest animal known to have ever lived.",
            funFact = "Male humpback whales sing long, complex songs. All the males in one region sing the same song, and it slowly changes from year to year.",
            secret = "Humpback whales can hunt as a team, blowing a ring of bubbles around a school of fish to trap it in a \"bubble net\", then lunging up through the middle.",
            image = R.drawable.fact_a06,
            credit = "National Marine Sanctuaries (public domain, Wikimedia Commons)",
        ),
        "a07" to Fact(
            about = "Crabs are crustaceans with a hard outer shell, ten legs and a pair of claws. Most live in the sea, but some live in rivers and lakes, and some on land.",
            funFact = "Most crabs walk sideways because of the way their legs bend. Moving sideways lets them scuttle away quickly.",
            secret = "Every year on Christmas Island, tens of millions of red crabs march from the forest to the sea to release their eggs.",
            image = R.drawable.fact_a07,
            credit = "Fernando Pérez Peralta (CC0, Wikimedia Commons)",
        ),
        "a08" to Fact(
            about = "Honey bees live in colonies of tens of thousands: one queen, many female workers and a few male drones. By carrying pollen from flower to flower, bees help many of our fruits and vegetables to grow.",
            funFact = "Honey bees tell each other where to find flowers with a \"waggle dance\". The angle and length of the dance show the direction and the distance.",
            secret = "A single worker bee makes only about one twelfth of a teaspoon of honey in her whole life.",
            image = R.drawable.fact_a08,
            credit = "John Severns (public domain, Wikimedia Commons)",
        ),
        "a09" to Fact(
            about = "Turtles are reptiles protected by a shell that is part of their skeleton. Sea turtles spend almost their whole lives at sea; the females come ashore only to lay their eggs on sandy beaches.",
            funFact = "For many turtles, the temperature of the sand decides whether the babies will be male or female. Warmer nests produce more females.",
            secret = "Female sea turtles often return to the very beach where they hatched to lay their own eggs, many years later.",
            image = R.drawable.fact_a09,
            credit = "Randall Ruiz ruizra (CC0, Wikimedia Commons)",
        ),
        "a10" to Fact(
            about = "Mice are small rodents found almost everywhere people live. They have front teeth that never stop growing, sensitive whiskers, and an excellent sense of hearing and smell.",
            funFact = "Male mice sing songs to attract females, but the songs are ultrasonic: too high-pitched for human ears to hear.",
            secret = "A mouse can squeeze through a gap about as wide as a pencil.",
            image = R.drawable.fact_a10,
            credit = "kcthetc1 (CC0, Wikimedia Commons)",
        ),

        // ---- Space & Sky --------------------------------------------------------------------
        "s01" to Fact(
            about = "A rocket flies by blasting hot gas out of its engines: the gas rushes one way and pushes the rocket the other. Rockets carry their own oxygen, so unlike planes they can keep working in space, where there is no air.",
            funFact = "The Saturn V rocket that carried astronauts to the Moon stood about 110 metres tall, taller than the Statue of Liberty.",
            secret = "To stay in orbit around the Earth, a spacecraft has to travel at about 28,000 km/h, fast enough to circle the whole planet in about 90 minutes.",
            image = R.drawable.fact_s01,
            credit = "NASA (public domain, Wikimedia Commons)",
        ),
        "s02" to Fact(
            about = "The Moon is Earth's only natural satellite, about a quarter as wide as the Earth. It shines by reflecting sunlight, and its gravity pulls on our oceans to make the tides.",
            funFact = "We always see the same side of the Moon, because it spins around exactly once each time it goes around the Earth.",
            secret = "The Moon is slowly drifting away from us, by about 3.8 centimetres every year.",
            image = R.drawable.fact_s02,
            credit = "Wikideas1 (CC0, Wikimedia Commons)",
        ),
        "s03" to Fact(
            about = "This ringed planet is Saturn, the sixth planet from the Sun and the second largest. It is a giant ball made mostly of hydrogen and helium, circled by bright rings of ice and rock.",
            funFact = "Saturn is less dense than water. If you could find a bathtub big enough, Saturn would float in it.",
            secret = "Saturn's main rings stretch about 270,000 km across, yet in many places they are only about ten metres thick.",
            image = R.drawable.fact_s03,
            credit = "NASA / JPL-Caltech / SSI (public domain, Wikimedia Commons)",
        ),
        "s04" to Fact(
            about = "The Sun is a star: a huge ball of hot gas, powered by nuclear fusion deep in its core. It holds about 99.8% of all the mass in our solar system.",
            funFact = "Sunlight takes about 8 minutes and 20 seconds to reach the Earth, so we always see the Sun as it was a few minutes ago.",
            secret = "About 1.3 million Earths could fit inside the Sun.",
            image = R.drawable.fact_s04,
            credit = "NASA/SDO (public domain, Wikimedia Commons)",
        ),
        "s05" to Fact(
            about = "Clouds form when warm, moist air rises and cools, and its water vapour turns into tiny droplets. When the droplets bump together and grow heavy enough, they fall as rain.",
            funFact = "A big fluffy cloud can weigh around 500 tonnes, as much as 100 elephants. It floats because its droplets are spread out across a huge space.",
            secret = "Raindrops are not tear-shaped. Small ones are round, and big ones flatten into a shape more like a hamburger bun as they fall.",
            image = R.drawable.fact_s05,
            credit = "W.carter (CC0, Wikimedia Commons)",
        ),
        "s06" to Fact(
            about = "UFO stands for \"unidentified flying object\": anything in the sky that people can't explain right away. Most sightings turn out to be planes, balloons, satellites, bright planets or unusual clouds.",
            funFact = "Smooth, saucer-shaped lenticular clouds, like the one in this photo, form over mountains, and they are often mistaken for flying saucers.",
            secret = "The words \"flying saucer\" became popular in 1947, after a pilot said that objects he saw near Mount Rainier moved like saucers skipping across water.",
            image = R.drawable.fact_s06,
            credit = "NOAA (public domain, Wikimedia Commons)",
        ),
        "s07" to Fact(
            about = "Stars are giant balls of glowing gas, like our Sun. The Pleiades in this photo is a cluster of young, hot stars about 440 light-years away, also known as the Seven Sisters.",
            funFact = "A star's colour shows its temperature: red stars are the coolest and blue-white stars are the hottest.",
            secret = "Looking at stars means looking back in time. The light you see from the Pleiades left those stars more than 400 years ago.",
            image = R.drawable.fact_s07,
            credit = "NASA, ESA, AURA/Caltech, Palomar Observatory (public domain, Wikimedia Commons)",
        ),
        "s08" to Fact(
            about = "Comets are icy leftovers from the birth of the solar system, often called \"dirty snowballs\". As a comet nears the Sun, its ice turns to gas, forming a glowing head and long tails.",
            funFact = "A comet's tail always points away from the Sun, pushed by sunlight and the solar wind. On its way back out, a comet travels tail-first.",
            secret = "Many meteor showers, like the Perseids every August, happen when the Earth passes through dust left behind by a comet.",
            image = R.drawable.fact_s08,
            credit = "WI-Photos (CC0, Wikimedia Commons)",
        ),
        "s09" to Fact(
            about = "Lightning is a giant electric spark. Inside a storm cloud, bits of ice and water crash together and build up electric charge, until it jumps to another cloud or down to the ground.",
            funFact = "A lightning bolt can heat the air around it to about 30,000 °C, around five times hotter than the surface of the Sun.",
            secret = "You can tell how far away lightning is: count the seconds until you hear the thunder. Every 3 seconds is about 1 kilometre.",
            image = R.drawable.fact_s09,
            credit = "C. Clark (public domain, Wikimedia Commons)",
        ),
        "s10" to Fact(
            about = "Astronauts are people trained to travel and work in space. On the International Space Station they live for months at a time, floating in microgravity while they do experiments.",
            funFact = "In 1984 astronaut Bruce McCandless, pictured here, became the first person to fly freely in space without a safety line, using a jetpack called the Manned Maneuvering Unit.",
            secret = "Astronauts can grow up to about 3% taller in space, because without gravity pressing down their spines stretch out.",
            image = R.drawable.fact_s10,
            credit = "NASA (public domain, Wikimedia Commons)",
        ),

        // ---- Masterworks --------------------------------------------------------------------
        "w01" to Fact(
            about = "Lighthouses are towers with a bright light at the top that guide ships at night and warn them away from rocks. Each lighthouse flashes its own pattern, so sailors can tell which one they are looking at.",
            funFact = "The Pharos of Alexandria in ancient Egypt, built more than 2,000 years ago, was one of the Seven Wonders of the Ancient World.",
            secret = "Many lighthouses use a Fresnel lens, invented in 1822. Its rings of glass prisms gather a lamp's light into a beam that can be seen more than 30 kilometres out at sea.",
            image = R.drawable.fact_w01,
            credit = "Cape Hatteras National Seashore (public domain, Wikimedia Commons)",
        ),
        "w02" to Fact(
            about = "Pines are evergreen trees with needle-shaped leaves in little bundles, and seeds that grow inside woody cones. There are more than 100 kinds, found mostly in the Northern Hemisphere.",
            funFact = "Some pine cones only open in the heat of a forest fire, dropping their seeds onto the freshly cleared ground.",
            secret = "A Great Basin bristlecone pine in California is nearly 5,000 years old, one of the oldest known living trees on Earth.",
            image = R.drawable.fact_w02,
            credit = "Treefender BC (CC0, Wikimedia Commons)",
        ),
        "w03" to Fact(
            about = "Crowns are worn by kings and queens as a symbol of their power. Royal crowns are often made of gold and set with precious stones, and are usually worn only on special occasions such as coronations.",
            funFact = "The British Imperial State Crown is set with more than 2,800 diamonds.",
            secret = "Some crowns are used for centuries. St Edward's Crown, which is placed on the head of British monarchs at their coronation, was made in 1661.",
            image = R.drawable.fact_w03,
            credit = "Unknown author (public domain, Wikimedia Commons)",
        ),
        "w04" to Fact(
            about = "Trophies are awards for winning a competition. The idea is ancient: winners at the games in ancient Athens received large painted jars filled with olive oil.",
            funFact = "The names of the winning players are engraved on ice hockey's Stanley Cup, so the trophy keeps growing a record of its champions.",
            secret = "The original football World Cup trophy was stolen in England in 1966. Days later it was found wrapped in newspaper under a hedge by a dog named Pickles.",
            image = R.drawable.fact_w04,
            credit = "ConifersOfGnarnia (CC0, Wikimedia Commons)",
        ),
        "w05" to Fact(
            about = "Coffee is made from the roasted seeds of the coffee plant, which grow inside small red berries. It is one of the most popular drinks in the world.",
            funFact = "A popular legend says coffee was discovered by an Ethiopian goat herder named Kaldi, who noticed his goats became lively after eating the berries.",
            secret = "Coffee beans aren't really beans at all. They are the seeds inside a fruit often called a coffee cherry.",
            image = R.drawable.fact_w05,
            credit = "mcfoodie (CC0, Wikimedia Commons)",
        ),
        "w06" to Fact(
            about = "Keys work with locks: the cuts along a key push tiny pins inside the lock to exactly the right heights, which lets the lock turn. Locks with pins go back thousands of years, to ancient Egypt.",
            funFact = "In ancient Rome, people often wore small keys as rings on their fingers, so they always had them with them.",
            secret = "The modern pin tumbler lock was perfected by Linus Yale Jr. in the 1860s, and his name can still be seen on many locks today.",
            image = R.drawable.fact_w06,
            credit = "lilmedia (CC0, Wikimedia Commons)",
        ),
        "w07" to Fact(
            about = "Mushrooms are the fruit of fungi, which mostly grow hidden underground as a web of fine threads. Fungi aren't plants: they are actually more closely related to animals.",
            funFact = "The red-and-white fly agaric is the classic storybook toadstool, but it is poisonous and should never be eaten.",
            secret = "One of the largest living things on Earth is a honey fungus in Oregon, USA. It spreads underground across almost 10 square kilometres.",
            image = R.drawable.fact_w07,
            credit = "George Chernilevsky (public domain, Wikimedia Commons)",
        ),
        "w08" to Fact(
            about = "Cacti are plants built for dry places. They store water in their thick stems, and their leaves have become spines, which protect them and help them lose less water. Almost all cacti come from the Americas.",
            funFact = "The saguaro cactus can grow more than 12 metres tall and live for over 150 years.",
            secret = "A saguaro often doesn't grow its first arm until it is 50 to 70 years old.",
            image = R.drawable.fact_w08,
            credit = "Gentry George, U.S. Fish and Wildlife Service (public domain, Wikimedia Commons)",
        ),
        "w09" to Fact(
            about = "Bells are usually cast from bronze and ring out when struck, with a sound that can carry a long way. For centuries they have called people together and marked the time.",
            funFact = "The Liberty Bell in Philadelphia, USA, is one of the most famous bells in the world, known for the large crack down its side.",
            secret = "The biggest bell in the world, the Tsar Bell in Moscow, weighs about 200 tonnes and has never been rung: a huge piece broke off before it could be hung.",
            image = R.drawable.fact_w09,
            credit = "Daderot (CC0, Wikimedia Commons)",
        ),
        "w10" to Fact(
            about = "A hot-air balloon floats because hot air is lighter than the cooler air around it. A burner heats the air inside the balloon, and the pilot goes up or down by adding heat or letting hot air out.",
            funFact = "The first passengers ever to fly in a hot-air balloon, in France in 1783, were a sheep, a duck and a rooster.",
            secret = "Balloon pilots can't steer left or right. Instead they rise or sink to find a wind blowing in the direction they want to go.",
            image = R.drawable.fact_w10,
            credit = "NoTalkMan (CC0, Wikimedia Commons)",
        ),
    )
}
