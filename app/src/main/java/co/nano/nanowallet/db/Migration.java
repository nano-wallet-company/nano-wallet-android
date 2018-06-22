package co.nano.nanowallet.db;

import android.support.annotation.NonNull;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

/**
 * Migration for Adding a UUID field to Realm
 */

public class Migration implements RealmMigration {

    @Override
    public void migrate(@NonNull DynamicRealm realm, long oldVersion, long newVersion) {
        RealmSchema schema = realm.getSchema();

        if (oldVersion == 1) {
            RealmObjectSchema credentialsSchema = schema.get("Credentials");
            if (credentialsSchema != null) {
                credentialsSchema.addField("uuid", String.class);
            }
            oldVersion++;
        }

        if (oldVersion == 2) {
            RealmObjectSchema credentialsSchema = schema.get("Credentials");
            if (credentialsSchema != null) {
                credentialsSchema.addField("pin", String.class);
            }
            oldVersion++;
        }

        if (oldVersion == 3) {
            RealmObjectSchema credentialsSchema = schema.get("Credentials");
            if (credentialsSchema != null) {
                credentialsSchema.addField("hasCompletedLegalAgreements", Boolean.class);
            }
            oldVersion++;
        }

        if (oldVersion == 4) {
            RealmObjectSchema credentialsSchema = schema.get("Credentials");
            if (credentialsSchema != null) {
                credentialsSchema.addField("hasAgreedToTracking", Boolean.class);
                credentialsSchema.addField("hasAnsweredAnalyticsQuestion", Boolean.class);
            }
            oldVersion++;
        }

        if (oldVersion == 5) {
            RealmObjectSchema credentialsSchema = schema.get("Credentials");
            if (credentialsSchema != null) {
                credentialsSchema.addField("seedIsSecure", Boolean.class);
            }
            oldVersion++;
        }

        if (oldVersion == 6) {
            RealmObjectSchema credentialsSchema = schema.get("Credentials");
            if (credentialsSchema != null) {
                credentialsSchema.addField("newlyGeneratedSeed", String.class);
                credentialsSchema.addField("hasSentToNewSeed", Boolean.class);
            }
            oldVersion++;
        }
    }

    @Override
    public int hashCode() {
        return 37;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof RealmMigration);
    }

}

