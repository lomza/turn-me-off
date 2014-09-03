package com.totemsoft.turnmeoff;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.TimePicker;

import com.totemsoft.turnmeoff.utils.Utils;

/**
 * Created with IntelliJ IDEA.
 * User: Antonina
 * Date: 17.11.13
 */
public class TimeDialogFragment extends DialogPreference {
    private int lastHour = Utils.getHour();
    private int lastMinute = Utils.getMinute();
    private boolean isAm = Utils.isAm();
    private TimePicker picker = null;
    private MySwitchEnabler switchEnabler;

    public TimeDialogFragment(Context context, AttributeSet attrs) {
        super(context, attrs);

        setPositiveButtonText(context.getString(R.string.positive_dialog_button));
        setNegativeButtonText(context.getString(R.string.negative_dialog_button));
    }

    private int get24Hour(String time) {
        if (time == null)
            return 0;

        String[] pieces = time.split(":");

        return Integer.valueOf(pieces[0]);
    }

    public static int getMinute(String time) {
        String[] pieces = time.split(":");
        String minPiece = pieces[1];
        minPiece = minPiece.substring(0, minPiece.indexOf(" "));

        return (Integer.parseInt(minPiece));
    }

    public MySwitchEnabler getSwitchEnabler() {
        return switchEnabler;
    }

    public boolean isSwitchOn() {
        return switchEnabler.isSwitchOn();
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);

        if (!isAm) {
            final int newHour = lastHour + 12;
            if (newHour <= 24)
                lastHour += 12;
        } else if (lastHour == 12)
            lastHour = 0;

        picker.setCurrentHour(lastHour);
        picker.setCurrentMinute(lastMinute);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        Switch switchh = (Switch) view.findViewById(R.id.switchWidget);
        switchEnabler = new MySwitchEnabler(getContext(), switchh, getKey() + C.KEY_PREF_SWITCH_SUFFIX);

        //if (switchh != null)
        //    switchh.setChecked(getSharedPreferences().getBoolean(getKey() + C.KEY_PREF_SWITCH_SUFFIX, false));

        /*switchh.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                getEditor().putBoolean(getKey() + C.KEY_PREF_SWITCH_SUFFIX, b).commit();
            }
        });*/
    }

    @Override
    protected View onCreateDialogView() {
        picker = new TimePicker(getContext());
        picker.setIs24HourView(false);

        return (picker);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            int h24Format = picker.getCurrentHour();
            lastHour = picker.getCurrentHour();
            lastMinute = picker.getCurrentMinute();

            String suffix = " AM";
            isAm = true;
            if (picker.getCurrentHour() == 0)
                lastHour = 12;
            else if (picker.getCurrentHour() == 12) {
                suffix = " PM";
                isAm = false;
                h24Format = 12;
            } else if (picker.getCurrentHour() > 12) {
                suffix = " PM";
                lastHour -= 12;
                isAm = false;
            }

            String time = String.valueOf(h24Format) + ":" + String.format("%02d", lastMinute) + suffix;

            if (callChangeListener(time)) {
                persistString(time);
            }
        }

        /*if (switchh != null)
            switchh.setChecked(getSharedPreferences().getBoolean(getKey() + "_switch", false));*/

    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return (a.getString(index));
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        String time;

        if (restoreValue) {
            if (defaultValue == null) {
                time = getPersistedString(Utils.getNowTime());
            } else {
                time = getPersistedString(defaultValue.toString());
            }
        } else {
            time = defaultValue.toString();
        }

        lastHour = get24Hour(time);
        lastMinute = getMinute(time);

        persistString(time);
    }

    public void setNowValue(String time) {
        if (time == null)
            return;

        lastHour = get24Hour(time);
        lastMinute = getMinute(time);

        persistString(time);
    }
}
