package com.totemsoft.turnmeoff;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;

/**
 * Created with IntelliJ IDEA.
 * User: Antonina
 * Date: 17.11.13
 */
public class TimeIntervalDialogFragment extends DialogPreference {
    private static final int DEFAULT_MIN_VALUE = 1;
    private static final int DEFAULT_MAX_VALUE = 60;
    private static final int DEFAULT_VALUE = 5;

    private int mMinValue;
    private int mMaxValue;
    private int mValue;
    private NumberPicker mNumberPicker;
    private MySwitchEnabler switchEnabler;

    public TimeIntervalDialogFragment(Context context) {
        this(context, null);
    }

    public TimeIntervalDialogFragment(Context context, AttributeSet attrs) {
        super(context, attrs);

        // get attributes specified in XML
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.NumberPickerDialogPreference, 0, 0);
        try {
            setMinValue(a.getInteger(R.styleable.NumberPickerDialogPreference_min, DEFAULT_MIN_VALUE));
            setMaxValue(a.getInteger(R.styleable.NumberPickerDialogPreference_android_max, DEFAULT_MAX_VALUE));
        } finally {
            a.recycle();
        }

        // set layout
        setDialogLayoutResource(R.layout.preference_time_interval_picker_dialog);
        setPositiveButtonText(context.getString(R.string.set));
        setNegativeButtonText(context.getString(R.string.negative_dialog_button));
        setDialogIcon(null);
    }

    public int getMaxValue() {
        return mMaxValue;
    }

    public int getMinValue() {
        return mMinValue;
    }

    public MySwitchEnabler getSwitchEnabler() {
        return switchEnabler;
    }

    public int getValue() {
        return mValue;
    }

    public boolean isSwitchOn() {
        return switchEnabler.isSwitchOn();
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        TextView dialogMessageText = (TextView) view.findViewById(R.id.text_dialog_message);
        dialogMessageText.setText(getDialogMessage());

        mNumberPicker = (NumberPicker) view.findViewById(R.id.number_picker);
        mNumberPicker.setMinValue(mMinValue);
        mNumberPicker.setMaxValue(mMaxValue);
        mNumberPicker.setValue(mValue);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        Switch switchh = (Switch) view.findViewById(R.id.switchWidget);
        switchEnabler = new MySwitchEnabler(getContext(), switchh, getKey() + C.KEY_PREF_SWITCH_SUFFIX);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        // when the user selects "OK", persist the new value
        if (positiveResult) {
            int numberPickerValue = mNumberPicker.getValue();
            if (callChangeListener(numberPickerValue)) {
                setValue(numberPickerValue);
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, DEFAULT_VALUE);
    }

    @Override
    protected void onSetInitialValue(boolean restore, Object defaultValue) {
        setValue(restore ? getPersistedInt(DEFAULT_VALUE) : (Integer) defaultValue);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        // check whether we saved the state in onSaveInstanceState()
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // didn't save the state, so call superclass
            super.onRestoreInstanceState(state);
            return;
        }

        // restore the state
        SavedState myState = (SavedState) state;
        setMinValue(myState.minValue);
        setMaxValue(myState.maxValue);
        setValue(myState.value);

        super.onRestoreInstanceState(myState.getSuperState());
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        // save the instance state so that it will survive screen orientation changes and other events that may temporarily destroy it
        final Parcelable superState = super.onSaveInstanceState();

        // set the state's value with the class member that holds current setting value
        final SavedState myState = new SavedState(superState);
        myState.minValue = getMinValue();
        myState.maxValue = getMaxValue();
        myState.value = getValue();

        return myState;
    }

    public void setMaxValue(int maxValue) {
        mMaxValue = maxValue;
        setValue(Math.min(mValue, mMaxValue));
    }

    public void setMinValue(int minValue) {
        mMinValue = minValue;
        setValue(Math.max(mValue, mMinValue));
    }

    public void setValue(int value) {
        value = Math.max(Math.min(value, mMaxValue), mMinValue);

        if (value != mValue) {
            mValue = value;
            persistInt(value);
            notifyChanged();
        }
    }

    private static class SavedState extends BaseSavedState {
        int minValue;
        int maxValue;
        int value;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public SavedState(Parcel source) {
            super(source);

            minValue = source.readInt();
            maxValue = source.readInt();
            value = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);

            dest.writeInt(minValue);
            dest.writeInt(maxValue);
            dest.writeInt(value);
        }

        @SuppressWarnings("unused")
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
