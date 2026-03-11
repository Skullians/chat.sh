package net.skullian.zenith.core.flavor;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Objects;

public record FlavorOptions(Logger logger, String mainPackage) {
    public FlavorOptions(@NotNull Logger logger, String mainPackage) {
        this.logger = logger;
        this.mainPackage = mainPackage;
    }

    public FlavorOptions(Logger logger) {
        this(logger, null);
    }

    @Override
    public String mainPackage() {
        return Objects.requireNonNull(this.mainPackage);
    }
}
