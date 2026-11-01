package com.fs.starfarer.api.alcoholism.config;

public enum Setting {

    CREW_PER_UNIT_PER_MONTH("alcoholism_CREW_PER_UNIT_PER_MONTH", 25d, 1d, 100d),
    BASE_ADDICTION_GAIN_PER_DAY("alcoholism_BASE_ADDICTION_GAIN_PER_DAY", 0.005d, 0.001d, 0.01d),
    MONTHLY_ADDICTION_GAIN_LIMIT_BEFORE_BLACKOUT("alcoholism_MONTHLY_ADDICTION_GAIN_LIMIT_BEFORE_BLACKOUT", 1d, 0.5d, 10d),
    WATER_MULT("alcoholism_WATER_MULT", 0.5d, 0.02d, 0.98d),
    ADJUST_OP_WHEN_OVER_LIMIT("alcoholism_ADJUST_OP_WHEN_OVER_LIMIT", true);

    public final String id;
    public final Object fallback;
    public final double min;
    public final double max;

    Setting(String id, double fallback, double min, double max) {
        this.id = id;
        this.fallback = fallback;
        this.min = min;
        this.max = max;
    }

    Setting(String id, boolean fallback) {
        this.id = id;
        this.fallback = fallback;
        this.min = 0;
        this.max = 1;
    }

    public Object sanitize(Object value) {
        if (fallback instanceof Boolean) return value instanceof Boolean ? value : fallback;
        if (!(value instanceof Number) || !Double.isFinite(((Number) value).doubleValue())) return fallback;
        return Math.max(min, Math.min(max, ((Number) value).doubleValue()));
    }

    public float value() {
        return ((Number) PlayerSettings.get(this)).floatValue();
    }

    public boolean enabled() {
        return (Boolean) PlayerSettings.get(this);
    }
}
