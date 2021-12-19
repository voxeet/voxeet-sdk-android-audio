package com.voxeet.audio.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public class __Opt<TYPE_PARAMETER> {

    @Nullable
    private final TYPE_PARAMETER ptr;

    private __Opt(@Nullable TYPE_PARAMETER parameter) {
        this.ptr = parameter;
    }

    @NonNull
    public static <TYPE_PARAMETER> __Opt<TYPE_PARAMETER> of(TYPE_PARAMETER parameter) {
        return new __Opt<>(parameter);
    }

    @NonNull
    public static <P, R> R of(P parameter, @NonNull Call<P, R> call, @NonNull R def) {
        return new __Opt<>(parameter).then(call).or(def);
    }

    @NonNull
    public static <P, R> Then<P, R> of(P parameter, @NonNull Call<P, R> call) {
        return new __Opt<>(parameter).then(call);
    }

    public static boolean isNonNull(@NonNull Object... v) {
        for (Object o : v) {
            if (null == o) return false;
        }
        return true;
    }

    @NonNull
    public <TYPE_RETURN> Then<TYPE_PARAMETER, TYPE_RETURN> then(@NonNull Call<TYPE_PARAMETER, TYPE_RETURN> call) {
        return new Then<>(ptr, call);
    }

    @NonNull
    public TYPE_PARAMETER or(@NonNull TYPE_PARAMETER parameter) {
        TYPE_PARAMETER value = ptr;
        if (null == value) return parameter;
        return value;
    }

    @Nullable
    public TYPE_PARAMETER orNull() {
        return ptr; //since it can be null ;)
    }

    public static class Then<TYPE_PARAMETER, TYPE_RETURN> {

        @NonNull
        private final Call<TYPE_PARAMETER, TYPE_RETURN> call;

        @Nullable
        private final TYPE_PARAMETER ptr;

        Then(@Nullable TYPE_PARAMETER ptr, @NonNull Call<TYPE_PARAMETER, TYPE_RETURN> call) {
            this.ptr = ptr;
            this.call = call;
        }

        @NonNull
        public <TYPE_RETURN_FOLLOWING> Then<TYPE_RETURN, TYPE_RETURN_FOLLOWING> then(@NonNull Call<TYPE_RETURN, TYPE_RETURN_FOLLOWING> call) {
            return new Then<>(orNull(), call);
        }

        @NonNull
        public TYPE_RETURN or(@NonNull TYPE_RETURN def) {
            TYPE_PARAMETER parameter = ptr;
            if (null == parameter) return def;
            TYPE_RETURN value = call.apply(parameter);
            if (null != value) return value;
            return def;
        }

        @Nullable
        public TYPE_RETURN orNull() {
            TYPE_PARAMETER parameter = ptr;
            if (null == parameter) return null;
            TYPE_RETURN value = call.apply(parameter);
            if (null != value) return value;
            return null;
        }
    }

    public interface Call<TYPE_PARAMETER, TYPE_RETURN> {
        TYPE_RETURN apply(@NonNull TYPE_PARAMETER parameter);
    }


}
