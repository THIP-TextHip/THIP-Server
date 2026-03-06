package konkuk.thip.common.aop;

public class FilterContextHolder {

    public enum FilterMode {
        ACTIVE_ONLY,
        UNFILTERED
    }

    private static final ThreadLocal<FilterMode> context =
            ThreadLocal.withInitial(() -> FilterMode.ACTIVE_ONLY);

    public static FilterMode get() {
        return context.get();
    }

    public static void set(FilterMode mode) {
        context.set(mode);
    }

    static void clear() {
        context.remove();
    }

    private FilterContextHolder() {}
}
