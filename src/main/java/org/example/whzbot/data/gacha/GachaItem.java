package org.example.whzbot.data.gacha;

import org.example.whzbot.data.IUser;
import org.example.whzbot.storage.json.Json;
import org.example.whzbot.storage.json.JsonNode;
import org.example.whzbot.storage.json.JsonObjectNode;

public class GachaItem {
    GachaPredicate predicate;
    String direction;
    int dir_type;
    int count_type;
    String store_path;
    boolean is_remain = false;

    public GachaItem(JsonObjectNode node) {
        JsonNode leaf;
        this.direction = Json.readString(node, "gacha", "");

        switch (Json.readString(node, "type", "")) {
            case "deck":
                this.dir_type = 1;
                break;
            case "gacha":
                this.dir_type = 2;
                break;
            default:
                this.dir_type = 0;
        }

        this.count_type = Json.readInt(node, "count_mode", 0);

        if (this.count_type != 0) {
            store_path = "gacha." + Json.readString(node, "store", "dummy");
        }

        leaf = node.get("prob");
        if (leaf instanceof JsonObjectNode) {
            JsonObjectNode obj_leaf = (JsonObjectNode) leaf;
            String predicate_type = Json.readString(obj_leaf, "predicate", "");
            switch (predicate_type) {
                case "prob_kinked": {
                    double base = Json.readDouble(obj_leaf, "base", 0);
                    int kink = Json.readInt(obj_leaf, "kink", 0);
                    double increment = Json.readDouble(obj_leaf, "increment", 0);
                    this.predicate = new GachaPredicateKinked(base, kink, increment);
                    break;
                }
                case "prob_const": {
                    this.predicate = new GachaPredicateConst(
                            Json.readDouble(obj_leaf, "prob", 0));
                    break;
                }
                case "prob_remain": {
                    this.predicate = new GachaPredicateRemain();
                    this.is_remain = true;
                    break;
                }
                default:
                    this.predicate = new GachaPredicateConst(0);
            }
        }
    }

    public GachaItem() {
        this.predicate = new GachaPredicateConst(0);
        this.direction = "";
        this.dir_type = 0;
        this.count_type = 0;
    }

    public boolean isRemain() {
        return this.is_remain;
    }

    /**
     * Return the direction type of this item.
     *
     * @return 0 = str, 1 = card_deck, 2 = gacha.
     */
    public int getType() {
        return this.dir_type;
    }

    public String getDirection() {
        return this.direction;
    }

    public double fallIn(double d, IUser user) {
        double para = 0;
        if (this.count_type > 0) {
            para = user.getCharacter().getGacha(this.store_path);
        }
        double prob = this.predicate.getProb(para, 0);
        return d - prob;
    }

    public void atGacha(IUser user) {
        if (this.count_type == 1) {
            user.getCharacter().resetGacha(this.store_path);
        }
    }

    public void atMiss(IUser user) {
        if (this.count_type > 0) {
            user.getCharacter().increaseGacha(this.store_path);
        }
    }
}
