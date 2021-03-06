package me.ccrama.redditslide.Activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.rey.material.widget.Slider;

import net.dean.jraw.paginators.Sorting;
import net.dean.jraw.paginators.TimePeriod;

import me.ccrama.redditslide.Authentication;
import me.ccrama.redditslide.Notifications.NotificationJobScheduler;
import me.ccrama.redditslide.R;
import me.ccrama.redditslide.Reddit;
import me.ccrama.redditslide.SettingValues;
import me.ccrama.redditslide.TimeUtils;
import me.ccrama.redditslide.Visuals.FontPreferences;
import me.ccrama.redditslide.Visuals.Palette;


/**
 * Created by ccrama on 3/5/2015.
 */
public class SettingsGeneral extends BaseActivity {
    public static void setupNotificationSettings(View dialoglayout, final Activity context) {
        if (Authentication.isLoggedIn) {
            final AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(context);
            final Slider landscape = (Slider) dialoglayout.findViewById(R.id.landscape);
            final CheckBox checkBox = (CheckBox) dialoglayout.findViewById(R.id.load);


            if (Reddit.notificationTime == -1) {
                checkBox.setChecked(false);
                checkBox.setText(context.getString(R.string.settings_mail_check));
            } else {
                checkBox.setChecked(true);
                landscape.setValue(Reddit.notificationTime / 15, false);
                checkBox.setText(context.getString(R.string.settings_notification,
                        TimeUtils.getTimeInHoursAndMins(Reddit.notificationTime, context.getBaseContext())));

            }
            landscape.setOnPositionChangeListener(new Slider.OnPositionChangeListener() {
                @Override
                public void onPositionChanged(Slider slider, boolean b, float v, float v1, int i, int i1) {
                    if (checkBox.isChecked())
                        checkBox.setText(context.getString(R.string.settings_notification,
                                TimeUtils.getTimeInHoursAndMins(i1 * 15, context.getBaseContext())));
                }
            });
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (!isChecked) {
                        Reddit.notificationTime = -1;
                        Reddit.seen.edit().putInt("notificationOverride", -1).apply();
                        checkBox.setText(context.getString(R.string.settings_mail_check));
                        landscape.setValue(0, true);
                        if (Reddit.notifications != null)
                            Reddit.notifications.cancel(context.getApplication());
                    } else {
                        Reddit.notificationTime = 60;
                        landscape.setValue(4, true);
                        checkBox.setText(context.getString(R.string.settings_notification,
                                TimeUtils.getTimeInHoursAndMins(Reddit.notificationTime, context.getBaseContext())));
                    }
                }
            });
            dialoglayout.findViewById(R.id.title).setBackgroundColor(Palette.getDefaultColor());
            //todo final Slider portrait = (Slider) dialoglayout.findViewById(R.id.portrait);

            //todo  portrait.setBackgroundColor(Palette.getDefaultColor());


            final Dialog dialog = builder.setView(dialoglayout).create();
            dialog.show();
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (checkBox.isChecked()) {
                        Reddit.notificationTime = landscape.getValue() * 15;
                        Reddit.seen.edit().putInt("notificationOverride", landscape.getValue() * 15).apply();
                        if (Reddit.notifications == null) {
                            Reddit.notifications = new NotificationJobScheduler(context.getApplication());
                        }
                        Reddit.notifications.cancel(context.getApplication());
                        Reddit.notifications.start(context.getApplication());
                    }
                }
            });
            dialoglayout.findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View d) {
                    if (checkBox.isChecked()) {
                        Reddit.notificationTime = landscape.getValue() * 15;
                        Reddit.seen.edit().putInt("notificationOverride", landscape.getValue() * 15).apply();
                        if (Reddit.notifications == null) {
                            Reddit.notifications = new NotificationJobScheduler(context.getApplication());
                        }
                        Reddit.notifications.cancel(context.getApplication());
                        Reddit.notifications.start(context.getApplication());
                        dialog.dismiss();
                        if (context instanceof Settings)
                            ((TextView) context.findViewById(R.id.notifications_current)).setText(
                                    context.getString(R.string.settings_notification_short,
                                            TimeUtils.getTimeInHoursAndMins(Reddit.notificationTime, context.getBaseContext())));
                    } else {
                        Reddit.notificationTime = -1;
                        Reddit.seen.edit().putInt("notificationOverride", -1).apply();
                        if (Reddit.notifications == null) {
                            Reddit.notifications = new NotificationJobScheduler(context.getApplication());
                        }
                        Reddit.notifications.cancel(context.getApplication());
                        dialog.dismiss();
                        if (context instanceof Settings)
                            ((TextView) context.findViewById(R.id.notifications_current)).setText(R.string.settings_notifdisabled);


                    }
                }
            });

        } else {
            new AlertDialogWrapper.Builder(context)

                    .setTitle(R.string.general_login)
                    .setMessage(R.string.err_login)
                    .setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            Intent inte = new Intent(context, Login.class);
                            context.startActivity(inte);
                        }
                    }).setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    context.finish();
                }
            }).show();
        }
    }


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyColorTheme();
        setContentView(R.layout.activity_settings_general);
        setupAppBar(R.id.toolbar, R.string.settings_title_general, true, true);

        {
            SwitchCompat single = (SwitchCompat) findViewById(R.id.single);

            single.setChecked(!Reddit.single);
            single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Reddit.single = !isChecked;
                    SettingValues.prefs.edit().putBoolean("Single", !isChecked).apply();

                }
            });
        }
        {
            SwitchCompat single = (SwitchCompat) findViewById(R.id.swipeback);

            single.setChecked(Reddit.swipeAnywhere);
            single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Reddit.swipeAnywhere = isChecked;
                    SettingValues.prefs.edit().putBoolean("swipeAnywhere", isChecked).apply();

                }
            });
        }

        {
            SwitchCompat single = (SwitchCompat) findViewById(R.id.scrollseen);

            single.setChecked(Reddit.scrollSeen);
            single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Reddit.scrollSeen = isChecked;
                    SettingValues.prefs.edit().putBoolean("scrollSeen", isChecked).apply();

                }
            });
        }


        /* Might need this later
        if (Reddit.expandedSettings) {
            {
                final SeekBar animationMultiplier = (SeekBar) findViewById(R.id.animation_length_sb);
                animationMultiplier.setProgress(Reddit.enter_animation_time_multiplier);
                animationMultiplier.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (progress <= 0) {
                            progress = 1;
                            animationMultiplier.setProgress(1);
                        }
                        SettingValues.prefs.edit().putInt("AnimationLengthMultiplier", progress).apply();
                        Reddit.enter_animation_time_multiplier = progress;
                        Reddit.enter_animation_time = Reddit.enter_animation_time_original * Reddit.enter_animation_time_multiplier;
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });

            }
        }
        else {
            findViewById(R.id.animation_length_sb).setVisibility(View.GONE);
            findViewById(R.id.enter_animation).setVisibility(View.GONE);
        }*/
        final SwitchCompat animation = (SwitchCompat) findViewById(R.id.animation);
        animation.setChecked(Reddit.animation);
        animation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Reddit.animation = isChecked;
                SettingValues.prefs.edit().putBoolean("Animation", isChecked).apply();
            }
        });
        {
            SwitchCompat single = (SwitchCompat) findViewById(R.id.exitcheck);

            single.setChecked(Reddit.exit);
            single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Reddit.exit = isChecked;
                    SettingValues.prefs.edit().putBoolean("Exit", isChecked).apply();

                }
            });
        }
        if (Reddit.notificationTime > 0) {
            ((TextView) findViewById(R.id.notifications_current)).setText(getString(R.string.settings_notification_short,
                    TimeUtils.getTimeInHoursAndMins(Reddit.notificationTime, getBaseContext())));

        } else {
            ((TextView) findViewById(R.id.notifications_current)).setText(R.string.settings_notifdisabled);
        }

        findViewById(R.id.notifications).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = getLayoutInflater();
                final View dialoglayout = inflater.inflate(R.layout.inboxfrequency, null);
                setupNotificationSettings(dialoglayout, SettingsGeneral.this);

            }
        });


        final TextView color = (TextView) findViewById(R.id.font);
        color.setText(new FontPreferences(this).getFontStyle().getTitle());
        findViewById(R.id.fontsize).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(SettingsGeneral.this, v);
                popup.getMenu().add("Large");
                popup.getMenu().add("Medium");
                popup.getMenu().add("Small");

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {

                        new FontPreferences(SettingsGeneral.this).setFontStyle(FontPreferences.FontStyle.valueOf(item.getTitle().toString()));
                        color.setText(new FontPreferences(SettingsGeneral.this).getFontStyle().getTitle());

                        return true;
                    }
                });

                popup.show();
            }
        });
        ((TextView) findViewById(R.id.sorting_current)).setText(Reddit.getSortingStrings(getBaseContext())[Reddit.getSortingId()]);

        {
            findViewById(R.id.sorting).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final DialogInterface.OnClickListener l2 = new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            switch (i) {
                                case 0:
                                    Reddit.defaultSorting = Sorting.HOT;
                                    break;
                                case 1:
                                    Reddit.defaultSorting = Sorting.NEW;
                                    break;
                                case 2:
                                    Reddit.defaultSorting = Sorting.RISING;
                                    break;
                                case 3:
                                    Reddit.defaultSorting = Sorting.TOP;
                                    Reddit.timePeriod = TimePeriod.HOUR;
                                    break;
                                case 4:
                                    Reddit.defaultSorting = Sorting.TOP;
                                    Reddit.timePeriod = TimePeriod.DAY;
                                    break;
                                case 5:
                                    Reddit.defaultSorting = Sorting.TOP;
                                    Reddit.timePeriod = TimePeriod.WEEK;
                                    break;
                                case 6:
                                    Reddit.defaultSorting = Sorting.TOP;
                                    Reddit.timePeriod = TimePeriod.MONTH;
                                    break;
                                case 7:
                                    Reddit.defaultSorting = Sorting.TOP;
                                    Reddit.timePeriod = TimePeriod.YEAR;
                                    break;
                                case 8:
                                    Reddit.defaultSorting = Sorting.TOP;
                                    Reddit.timePeriod = TimePeriod.ALL;
                                    break;
                                case 9:
                                    Reddit.defaultSorting = Sorting.CONTROVERSIAL;
                                    Reddit.timePeriod = TimePeriod.HOUR;
                                    break;
                                case 10:
                                    Reddit.defaultSorting = Sorting.CONTROVERSIAL;
                                    Reddit.timePeriod = TimePeriod.DAY;
                                    break;
                            }
                            SettingValues.prefs.edit().putString("defaultSorting", Reddit.defaultSorting.name()).apply();
                            SettingValues.prefs.edit().putString("timePeriod", Reddit.timePeriod.name()).apply();
                            SettingValues.defaultSorting = Reddit.defaultSorting;
                            SettingValues.timePeriod = Reddit.timePeriod;
                            ((TextView) findViewById(R.id.sorting_current)).setText(
                                    Reddit.getSortingStrings(getBaseContext())[Reddit.getSortingId()]);
                        }
                    };
                    AlertDialogWrapper.Builder builder = new AlertDialogWrapper.Builder(SettingsGeneral.this);
                    builder.setTitle(R.string.sorting_choose);
                    builder.setSingleChoiceItems(
                            Reddit.getSortingStrings(getBaseContext()), Reddit.getSortingId(), l2);
                    builder.show();
                }
            });
        }

    }

}