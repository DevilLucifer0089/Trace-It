package com.lostandfound.utils;

import com.lostandfound.models.Item;
import java.util.ArrayList;
import java.util.List;

public class DummyDataUtils {

    public static List<Item> getDummyItems(String currentUserId) {
        List<Item> dummyList = new ArrayList<>();

        dummyList.add(new Item(
                currentUserId,
                "Blue Nike Backpack",
                "Left it near the library entrance. Contains important textbooks and a blue pencil case.",
                "Others",
                Constants.STATUS_LOST,
                "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&q=80&w=400",
                "June 12, 2025",
                "Main Library, Sector 7",
                0.0, 0.0,
                "Demo User", ""
        ));

        dummyList.add(new Item(
                "other_user_1",
                "iPhone 14 Pro - Space Black",
                "Found this phone on a bench near the cafeteria. It is locked with a passcode.",
                "Electronics",
                Constants.STATUS_FOUND,
                "https://images.unsplash.com/photo-1663499482523-1c0c1bae4ce1?auto=format&fit=crop&q=80&w=400",
                "June 11, 2025",
                "Central Cafeteria",
                0.0, 0.0,
                "Sarah Miller", "https://i.pravatar.cc/150?u=sarah"
        ));

        dummyList.add(new Item(
                currentUserId,
                "Golden Retriever Puppy",
                "Responding to the name 'Buddy'. Wearing a red collar. Lost near the park.",
                "Pets",
                Constants.STATUS_LOST,
                "https://images.unsplash.com/photo-1552053831-71594a27632d?auto=format&fit=crop&q=80&w=400",
                "June 10, 2025",
                "Riverside Park",
                0.0, 0.0,
                "Demo User", ""
        ));

        dummyList.add(new Item(
                "other_user_2",
                "Bunch of Keys",
                "Found a set of 3 keys with a wooden keychain. Looks like house keys.",
                "Keys",
                Constants.STATUS_FOUND,
                "https://images.unsplash.com/photo-1582139329536-e7284fece509?auto=format&fit=crop&q=80&w=400",
                "June 10, 2025",
                "Parking Lot B",
                0.0, 0.0,
                "Mike Ross", "https://i.pravatar.cc/150?u=mike"
        ));

        dummyList.add(new Item(
                currentUserId,
                "Wallet - Brown Leather",
                "Found a brown leather wallet near the bus stop. No cash, only some ID cards.",
                "Others",
                Constants.STATUS_FOUND,
                "https://images.unsplash.com/photo-1627123430984-7151107d3bca?auto=format&fit=crop&q=80&w=400",
                "June 09, 2025",
                "West Bus Terminal",
                0.0, 0.0,
                "Demo User", ""
        ));

        Item resolvedItem = new Item(
                currentUserId,
                "Black Umbrella",
                "Left it in the classroom. Reunited with owner.",
                "Others",
                Constants.STATUS_LOST,
                "https://images.unsplash.com/photo-1536647464919-61884144358a?auto=format&fit=crop&q=80&w=400",
                "June 05, 2025",
                "Room 302",
                0.0, 0.0,
                "Demo User", ""
        );
        resolvedItem.setResolved(true);
        dummyList.add(resolvedItem);

        Item resolvedItem2 = new Item(
                currentUserId,
                "Water Bottle",
                "Found near the gym. Returned to owner.",
                "Others",
                Constants.STATUS_FOUND,
                "https://images.unsplash.com/photo-1602143399827-70ce6f636a00?auto=format&fit=crop&q=80&w=400",
                "June 04, 2025",
                "Campus Gym",
                0.0, 0.0,
                "Demo User", ""
        );
        resolvedItem2.setResolved(true);
        dummyList.add(resolvedItem2);

        return dummyList;
    }
}
