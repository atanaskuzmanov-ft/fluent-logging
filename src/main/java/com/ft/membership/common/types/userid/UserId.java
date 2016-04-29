package com.ft.membership.common.types.userid;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An immutable value type representing the globally unique identity (GUID) for FT Users.
 *
 * Essentially wraps a UUID value to provide a more fluent domain model.
 */
public final class UserId {

    private final UUID id;

    /**
     * Static factory method for user identities
     * @param id A UUID to be used as the basis for unique user identity.
     * @return A UserId object corresponding to the param id
     * @throws NullPointerException Thrown when the supplied id is null
     */
    public static UserId userId(UUID id) {
        return new UserId(id);
    }

    /**
     * Static factory method for generating a random user identity
     * @return A UserId object with a random uuid
     */
    public static UserId randomUserId() {
        return userId(UUID.randomUUID());
    }

    /**
     * @param id A UUID to be used as the basis for unique user identity.
     * @throws NullPointerException Thrown when the supplied id is null
     */
    @JsonCreator
    public UserId(UUID id) {
        this.id = checkNotNull(id, "Cannot create a UserId with null UUID value");
    }

    @JsonValue
    public UUID asUUID() {
        return id;
    }

    @Override
    public String toString() {
        return id.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        UserId that = (UserId) obj;
        return Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}