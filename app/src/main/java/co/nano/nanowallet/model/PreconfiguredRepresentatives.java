package co.nano.nanowallet.model;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import timber.log.Timber;

/**
 * Preconfigured representatives to choose from
 */

public class PreconfiguredRepresentatives {
    private static List<String> representatives = Arrays.asList(
            "xrb_3arg3asgtigae3xckabaaewkx3bzsh7nwz7jkmjos79ihyaxwphhm6qgjps4",
            "xrb_1stofnrxuz3cai7ze75o174bpm7scwj9jn3nxsn8ntzg784jf1gzn1jjdkou",
            "xrb_1q3hqecaw15cjt7thbtxu3pbzr1eihtzzpzxguoc37bj1wc5ffoh7w74gi6p",
            "xrb_3dmtrrws3pocycmbqwawk6xs7446qxa36fcncush4s1pejk16ksbmakis78m",
            "xrb_3hd4ezdgsp15iemx7h81in7xz5tpxi43b6b41zn3qmwiuypankocw3awes5k",
            "xrb_1anrzcuwe64rwxzcco8dkhpyxpi8kd7zsjc1oeimpc3ppca4mrjtwnqposrs",
            "xrb_1hza3f7wiiqa7ig3jczyxj5yo86yegcmqk3criaz838j91sxcckpfhbhhra1",
            "xrb_3crzecs58y9gd1ucqcfcdsh56ywty5ixzqk41oa5d3i1ggm4bd6c9q5u34m3",
            "xrb_1w77aapnijnm5mo16r3xtpqu7n459r61fqpcdt3kxfmz8gtqgzbozswxmduy",
            "xrb_1n4dtjocgft8aexcr5ftiibu9f47g3j13ay8yzx35qk3qe6sfdq16nsnu5h3",
            "xrb_3ju9wdb8zpzdn3bnhz6ypm5sipccok9urqif9kdyweimj9c9zyi54ceos1qu",
            "xrb_1nanode8ngaakzbck8smq6ru9bethqwyehomf79sae1k7xd47dkidjqzffeg"
    );

    public static String getRepresentative() {
        int index = new Random().nextInt(representatives.size());
        String rep = representatives.get(index);
        Timber.d("Representative: %s", rep);
        return rep;
    }
}
