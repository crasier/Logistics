package com.sdeport.logistics.common.widgets;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.sdeport.logistics.common.R;
import com.sdeport.logistics.common.utils.LocalDisplay;

/**
 * 自定义Dialog
 */
public class CustomDialog extends Dialog {

    // 默认的样式
    public static final int DIALOG_STYLE_DEFAULT = 0;
    // 样式一
    public static final int DIALOG_STYLE_CRY = 1;
    // 标题
    public TextView title;
    private View layout;

    public CustomDialog(Context context) {
        super(context);
    }

    public CustomDialog(Context context, int theme) {
        super(context, theme);
    }

    public void setCustomTitle(int resid) {
        title.setText(resid);
    }

    public void setCustomTitle(CharSequence text) {
        title.setText(text);
    }

    public void setCustomMessage(int resid) {
        if (layout.findViewById(R.id.tv_message) != null) {
            ((TextView) layout.findViewById(R.id.tv_message)).setText(resid);
        }
    }

    public void setCustomMessage(CharSequence text) {
        if (layout.findViewById(R.id.tv_message) != null) {
            ((TextView) layout.findViewById(R.id.tv_message)).setText(text);
        }
    }

    /**
     * 设置positive button
     *
     * @param text
     * @param listener
     */
    public void setButton(CharSequence text,
                          OnClickListener listener) {
        ((Button) layout.findViewById(R.id.btn_positive)).setText(text);
        final OnClickListener positiveListener = listener;
        if (positiveListener != null) {
            layout.findViewById(R.id.btn_positive)
                    .setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            positiveListener.onClick(CustomDialog.this,
                                    DialogInterface.BUTTON_POSITIVE);
                        }
                    });
        }
    }

    /**
     * 设置positive button
     *
     * @param id
     * @param listener
     */
    public void setButton(int id,
                          OnClickListener listener) {
        ((Button) layout.findViewById(R.id.btn_positive)).setText(id);
        final OnClickListener positiveListener = listener;
        if (positiveListener != null) {
            layout.findViewById(R.id.btn_positive)
                    .setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            positiveListener.onClick(CustomDialog.this,
                                    DialogInterface.BUTTON_POSITIVE);
                        }
                    });
        }
    }


    //临时增加，用来动态修改按钮文字
    public void setPositiveButton(int id) {
        Button posBtn = (Button) layout.findViewById(R.id.btn_positive);
        if (id == 0) {
            posBtn.setVisibility(View.GONE);
            return;
        }

        posBtn.setVisibility(View.VISIBLE);
        posBtn.setText(id);
    }

    //临时增加，用来动态修改按钮文字
    public void setNegativeButton(int id) {

        Button negBtn = (Button) layout.findViewById(R.id.btn_negative);
        if (id == 0) {
            negBtn.setVisibility(View.GONE);
            return;
        }
        negBtn.setVisibility(View.VISIBLE);
        negBtn.setText(id);
    }

    public void setView(View view) {
        layout = view;
    }

    public static class Builder {
        private Context context;
        private String title;
        private String message;
        private String positiveButtonText;
        private String negativeButtonText;
        private String neutralButtonText;
        private View contentView;
        private OnClickListener positiveButtonClickListener;
        private OnClickListener negativeButtonClickListener;
        private OnClickListener neutralButtonClickListener;

        // 标题左侧的图标(默认不显示)
        private int mTitleIconResId = -1;
        // 布局的样式
        private int mViewStyle = DIALOG_STYLE_DEFAULT;

        public Builder(Context context) {
            this.context = context;
        }

        /**
         * Set the view's style
         *
         * @param style
         * @return
         */
        public Builder setViewStyle(int style) {
            this.mViewStyle = style;
            return this;
        }

        /**
         * Set the title's icon res id
         *
         * @param resId
         * @return
         */
        public Builder setTitleIcon(int resId) {
            this.mTitleIconResId = resId;
            return this;
        }

        /**
         * Set the Dialog message from String
         *
         * @param message
         * @return
         */
        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        /**
         * Set the Dialog message from resource
         *
         * @param message
         * @return
         */
        public Builder setMessage(int message) {
            this.message = (String) context.getText(message);
            return this;
        }

        /**
         * Set the Dialog title from resource
         *
         * @param title
         * @return
         */
        public Builder setTitle(int title) {
            this.title = (String) context.getText(title);
            return this;
        }

        /**
         * Set the Dialog title from String
         *
         * @param title
         * @return
         */

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setContentView(View v) {
            this.contentView = v;
            return this;
        }

        /**
         * Set the positive button resource and it's listener
         *
         * @param positiveButtonText
         * @return
         */
        public Builder setPositiveButton(int positiveButtonText,
                                         OnClickListener listener) {
            this.positiveButtonText = (String) context
                    .getText(positiveButtonText);
            this.positiveButtonClickListener = listener;
            return this;
        }

        public Builder setPositiveButton(String positiveButtonText,
                                         OnClickListener listener) {
            this.positiveButtonText = positiveButtonText;
            this.positiveButtonClickListener = listener;
            return this;
        }

        /**
         * Set the negative button resource and it's listener
         *
         * @param negativeButtonText
         * @return
         */
        public Builder setNegativeButton(int negativeButtonText,
                                         OnClickListener listener) {
            this.negativeButtonText = (String) context
                    .getText(negativeButtonText);
            this.negativeButtonClickListener = listener;
            return this;
        }

        public Builder setNegativeButton(String negativeButtonText,
                                         OnClickListener listener) {
            this.negativeButtonText = negativeButtonText;
            this.negativeButtonClickListener = listener;
            return this;
        }

        /**
         * Set the neutral button resource and it's listener
         *
         * @param neutralButtonText
         * @return
         */
        public Builder setNeutralButton(int neutralButtonText,
                                        OnClickListener listener) {
            this.neutralButtonText = (String) context
                    .getText(neutralButtonText);
            this.neutralButtonClickListener = listener;
            return this;
        }

        public Builder setNeutralButton(String neutralButtonText,
                                        OnClickListener listener) {
            this.neutralButtonText = neutralButtonText;
            this.neutralButtonClickListener = listener;
            return this;
        }

        public CustomDialog create() {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // instantiate the dialog with the custom Theme
            final CustomDialog dialog = new CustomDialog(context,
                    R.style.customDialog);
            /**
             * dialog 默认的样式@android:style/Theme.Dialog 对应的style有pading属性,所以
             * win.getDecorView().setPadding(0, 0, 0, 0); 就能够水平占满了
             */
            Window win = dialog.getWindow();
            win.getDecorView().setPadding(0, 0, 0, 0);
            WindowManager.LayoutParams lp = win.getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            win.setAttributes(lp);

            // 解析布局
            int layoutResId = R.layout.dialog_custom_common;
//            if (mViewStyle == DIALOG_STYLE_CRY) {
//                layoutResId = R.layout.dialog_custom_negative;
//            }
            View layout = inflater.inflate(layoutResId, null);
            dialog.setView(layout);
            // set the dialog title
            dialog.title = layout.findViewById(R.id.tv_title);
            dialog.title.setText(title);

            // 设置标题左侧的图标
            if (mTitleIconResId != -1) {
                Drawable icon = context.getResources().getDrawable(
                        mTitleIconResId);
                icon.setBounds(0, 0, icon.getIntrinsicWidth(),
                        icon.getIntrinsicHeight());
                Drawable[] drawable = dialog.title.getCompoundDrawables();
                dialog.title.setCompoundDrawables(icon, drawable[1],
                        drawable[2], drawable[3]);
                dialog.title.setCompoundDrawablePadding(LocalDisplay.dp2px(6));
            }

            // set the confirm button
            if (positiveButtonText != null) {
                ((Button) layout.findViewById(R.id.btn_positive))
                        .setText(positiveButtonText);
                if (positiveButtonClickListener != null) {
                    layout.findViewById(R.id.btn_positive)
                            .setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    positiveButtonClickListener.onClick(dialog,
                                            DialogInterface.BUTTON_POSITIVE);
                                }
                            });
                }
            } else {
                // if no confirm button just set the visibility to GONE
                layout.findViewById(R.id.btn_positive).setVisibility(View.GONE);
            }

            // set the cancel button
            if (negativeButtonText != null) {
                ((Button) layout.findViewById(R.id.btn_negative))
                        .setText(negativeButtonText);
                if (negativeButtonClickListener != null) {
                    layout.findViewById(R.id.btn_negative)
                            .setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    negativeButtonClickListener.onClick(dialog,
                                            DialogInterface.BUTTON_NEGATIVE);
                                }
                            });
                }
            } else {
                // if no cancel button just set the visibility to GONE
                layout.findViewById(R.id.btn_negative).setVisibility(View.GONE);
            }

            // set the detail button
            if (neutralButtonText != null) {
                ((Button) layout.findViewById(R.id.btn_neutral))
                        .setText(neutralButtonText);
                if (neutralButtonClickListener != null) {
                    layout.findViewById(R.id.btn_neutral)
                            .setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    neutralButtonClickListener.onClick(dialog,
                                            DialogInterface.BUTTON_NEUTRAL);
                                }
                            });
                }
            } else {
                // if no detail button just set the visibility to GONE
                layout.findViewById(R.id.btn_neutral).setVisibility(View.GONE);
            }

            // set the content message
            if (message != null) {
                ((TextView) layout.findViewById(R.id.tv_message))
                        .setText(message);
                if (message.length() > 100) {
                    ScrollView messageScrollView = (layout.findViewById(R.id.msg_scrollview));
                    LinearLayout.LayoutParams msgParams = new
                            LinearLayout.LayoutParams(
                            LayoutParams.WRAP_CONTENT, 400);
                    messageScrollView.setLayoutParams(msgParams);
                }

            } else if (contentView != null) {
                // if no message set
                // add the contentView to the dialog body
                ((LinearLayout) layout.findViewById(R.id.ll_content))
                        .removeAllViews();
                ((LinearLayout) layout.findViewById(R.id.ll_content)).addView(
                        contentView, new LayoutParams(
                                LayoutParams.MATCH_PARENT,
                                LayoutParams.MATCH_PARENT));
            }

            dialog.setContentView(layout);
            return dialog;
        }
    }
}
