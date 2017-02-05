package com.example.shivamgandhi.recoglass;

import android.graphics.Bitmap;

import com.microsoft.projectoxford.face.contract.Person;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by James on 2/5/2017.
 */

public class PersonModel {
    private static Map<String, PersonModel> personMap = new HashMap();
    private String name;
    private Detection det;
    private String personGroupId;
    private static UUID non_initialized = UUID.randomUUID();
    private UUID personId = non_initialized;

    public PersonModel(String name, Detection det, String personGroupId) {
        this.name = name;
        this.det = det;
        this.personGroupId = personGroupId;

    }
    // TODO: IMPORTANT: MAKE THE PERSONMAP PERSIST ON APP RELOAD, OR RETRIEVE FROM MS
    public static void getPerson(String name, Detection det, String personGroupId, Consumer<PersonModel> personModelCallback) {
        if (personMap.containsKey(name)) {
            personModelCallback.accept(personMap.get(name));
        }
        else {

            PersonModel.createPerson(name, det, personGroupId, personModelCallback);
        }

    }

    public static void createPerson(String name, Detection det, String personGroupId, Consumer<PersonModel> personModelCallback) {
        final PersonModel p = new PersonModel(name, det, personGroupId);
        det.createPerson(personGroupId, name, "", new Consumer<UUID>(){
            public void accept(UUID u) {
                p.personId = u;
            }
        });
        personMap.put(name, p);
        personModelCallback.accept(p);
    }

    public static boolean exists(String name) {
        return personMap.containsKey(name);
    }

    public UUID getPersonId() {
        return personId;
    }

    public void addFace(Bitmap bmp) {
        det.addPersonFace(personGroupId, this.getPersonId(), "", Detection.bitmapToByteArray(bmp), new Consumer<UUID>(){public void accept(UUID u){}});
    }

    public static Map<String, String> getPersonMap() {
        Map<String, String> ret = new HashMap();
        for (Map.Entry<String, PersonModel> e : personMap.entrySet()) {
            ret.put(e.getKey(), e.getValue().getPersonId().toString());
        }
        return ret;
    }

    public static void updatePersonMap(Map<String, String> uuids, Detection det, String personGroupId) {
        if (true) return;
        for (Map.Entry<String, String> map : uuids.entrySet()) {
            PersonModel p = new PersonModel(map.getKey(), det, personGroupId);
            p.personId = UUID.fromString(map.getValue());
            personMap.put(map.getKey(), p);
        }
    }
}
