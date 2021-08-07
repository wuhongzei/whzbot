package org.example.whzbot.storage;

import java.util.ArrayList;

import org.example.whzbot.helper.RandomHelper;
import org.example.whzbot.helper.TranslateHelper;

public class CardDeck {
    public static TranslateHelper draw(String deck_name) {
        String[] deck_set = GlobalVariable.CARD_DECK.get(deck_name);
        if (deck_set == null)
            return new TranslateHelper("deckNotFound", 1);
        return execCard(RandomHelper.drawFromArray(deck_set));
    }

    public static TranslateHelper[] draw(String deck_name, int count) {
        String[] deck_set = GlobalVariable.CARD_DECK.get(deck_name);
        if (deck_set == null)
            return new TranslateHelper[]{new TranslateHelper("deckNotFound", 1)};

        int deck_size = deck_set.length;
        String[] cards;
        if (deck_size == 1) {
            cards = new String[count];
            String card = deck_set[0];
            for (int i = 0; i < count; i++) {
                cards[i] = card;
            }
        }
        else {
            cards = RandomHelper.drawFromArray(deck_set, count);
        }

        TranslateHelper[] rtn;
        if (count > deck_size && deck_size != 1) {
            count = deck_size;
            rtn = new TranslateHelper[count + 1];
            rtn[deck_size] = new TranslateHelper("deckEmpty", 1);
        }
        else {
            rtn = new TranslateHelper[count];
        }
        for (int i = 0; i < count; i++) {
            rtn[i] = execCard(cards[i]);
        }
        return rtn;
    }

    public static TranslateHelper execCard(String card) {
        int index_front = card.indexOf('{');
        int index_back = 0;
        ArrayList<TranslateHelper> replacements = new ArrayList<>();
        StringBuilder new_card = new StringBuilder();
        while (index_front != -1) {
            new_card.append(card.substring(index_back, index_front));

            index_back = card.indexOf('}', index_front + 1);
            if (index_back == -1)
                break;
            String replace_card = card.substring(index_front + 1, index_back);
            new_card.append(String.format("{%d}", replacements.size()));
            replacements.add(draw(replace_card));

            index_back++;
            index_front = card.indexOf('{', index_back + 1);
        }
        if (index_front == -1)
            new_card.append(card.substring(index_back));
        else
            new_card.append(card.substring(index_front));

        return new TranslateHelper(
                new_card.toString(),
                replacements.toArray(new TranslateHelper[0]),
                3
        );
    }
}
