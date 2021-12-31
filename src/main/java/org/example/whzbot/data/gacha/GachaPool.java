package org.example.whzbot.data.gacha;

import org.example.whzbot.data.IUser;
import org.example.whzbot.data.User;
import org.example.whzbot.storage.json.JsonListNode;
import org.example.whzbot.storage.json.JsonNode;
import org.example.whzbot.storage.json.JsonObjectNode;

import java.util.Random;


public class GachaPool {
    GachaItem[] pool;
    String name;

    public GachaPool(JsonListNode node) {
        this.pool = new GachaItem[node.size()];
        this.name = node.getName();

        GachaItem remain = null;
        GachaItem temp;
        int index = 0;
        for (JsonNode leaf : node) {
            if (leaf instanceof JsonObjectNode) {
                temp = new GachaItem((JsonObjectNode) leaf);
                if (temp.isRemain()) {
                    remain = temp;
                } else {
                    this.pool[index] = temp;
                    index++;
                }
            } else {
                System.err.printf("Unrecognized gacha item in pool %s@%d\n",
                        this.name, index);
                this.pool[index] = new GachaItem();
                index++;
            }
        }
        if (remain != null) {
            pool[index] = remain;
        }
    }

    public String getName() {
        return this.name;
    }

    public GachaItem gacha(IUser user) {
        Random rnd = new Random();
        double d = rnd.nextDouble();

        int i = -1;
        while (i < this.pool.length-1 && d > 0) {
            i++;
            d = this.pool[i].fallIn(d, user);
        }
        for(int j = 0;j <this.pool.length; j++) {
            if (j == i)
                this.pool[j].atGacha(user);
            else
                this.pool[j].atMiss(user);
        }
        return this.pool[i];
    }
}
