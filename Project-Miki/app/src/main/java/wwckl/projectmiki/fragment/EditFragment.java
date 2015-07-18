package wwckl.projectmiki.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.math.BigDecimal;

import wwckl.projectmiki.R;
import wwckl.projectmiki.activity.BillSplitterActivity;
import wwckl.projectmiki.activity.SettingsActivity;
import wwckl.projectmiki.models.Bill;
import wwckl.projectmiki.models.Item;
import wwckl.projectmiki.models.Receipt;

/**
 * Created by Aryn on 7/12/15.
 */
public class EditFragment extends Fragment {
    final String PARCEL_BILL = "PARCEL_BILL";

    private View mView;
    private TableLayout mLayoutEditItems;
    private TableLayout mLayoutEditTotals;
    private BillSplitterActivity mBillSplitterActivity;
    private Bill mBill;
    private EditText mSubtotal;
    private EditText mGST;
    private EditText mGstPercent;
    private EditText mSVC;
    private EditText mSvcPercent;
    private EditText mTotal;
    private CheckBox mUseSubtotalsCheckBox;
    private ImageButton mAddItemButton;
    private ImageView mReceiptImageView;
    private ScrollView mReceiptScrollView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_edit, container, false);

        // get views
        mReceiptImageView = (ImageView) mView.findViewById(R.id.imgvReceipt);
        mReceiptScrollView = (ScrollView) mView.findViewById(R.id.scrollImage);
        // get edit fields
        mLayoutEditItems = (TableLayout) mView.findViewById(R.id.layoutEditItems);
        mLayoutEditTotals = (TableLayout) mView.findViewById(R.id.layoutEditTotals);
        mSubtotal = (EditText) mLayoutEditTotals.findViewById(R.id.etSubTotal);
        mGST = (EditText) mLayoutEditTotals.findViewById(R.id.etGST);
        mGstPercent = (EditText) mLayoutEditTotals.findViewById(R.id.etGstPercent);
        mSVC = (EditText) mLayoutEditTotals.findViewById(R.id.etSVC);
        mSvcPercent = (EditText) mLayoutEditTotals.findViewById(R.id.etSvcPercent);
        mTotal = (EditText) mLayoutEditTotals.findViewById(R.id.etTotal);

        // Set up UseSubtotals checkbox
        mUseSubtotalsCheckBox = (CheckBox) mView.findViewById(R.id.cbUseSubtotals);
        mUseSubtotalsCheckBox.setChecked(!mBill.getUseSubtotals());
        mUseSubtotalsCheckBox.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mBill.setUseSubtotals(!mUseSubtotalsCheckBox.isChecked());
            }
        });

        // setup Add Item onClickListener
        mAddItemButton = (ImageButton) mView.findViewById(R.id.btnAddItem);
        mAddItemButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addNewItem();
            }
        });

        // setup onFocusChangeListener
        setOnFocusChangeListener(mSubtotal);
        setOnFocusChangeListener(mGST);
        setOnFocusChangeListener(mGstPercent);
        setOnFocusChangeListener(mSVC);
        setOnFocusChangeListener(mSvcPercent);
        setOnFocusChangeListener(mTotal);

        createItems();
        updateTotals();

        // add Image if present
        if (Receipt.getReceiptBitmap() != null) {
            mReceiptImageView.setImageBitmap(Receipt.getReceiptBitmap());
            mReceiptScrollView.setVisibility(View.VISIBLE);
            Log.d("Receipt bitmap", "not null");
        }
        else {
            mReceiptScrollView.setVisibility(View.GONE);
            Log.d("Receipt bitmap", "null");
        }
        return mView;
    }

    @Override
    public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
        menu.clear();
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_edit, menu);
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
        // Action bar menu.
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent settingsIntent = new Intent(this.getActivity(), SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.action_help:
                Intent myWebLink = new Intent(android.content.Intent.ACTION_VIEW);
                myWebLink.setData(Uri.parse("https://github.com/WomenWhoCode/KL-network/wiki/Project-Miki-Help-File"));
                startActivity(myWebLink);
                return true;
            case R.id.action_done:
                if (mBillSplitterActivity != null) {
                    mBillSplitterActivity.setActivityBill(mBill);
                    mBillSplitterActivity.fragmentSuicide();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof BillSplitterActivity) {
            mBillSplitterActivity = (BillSplitterActivity) activity;
            mBill = mBillSplitterActivity.getActivityBill();
        }
        else {
            mBill = new Bill("");
        }
    }

    private void setOnFocusChangeListener(EditText editText){

        editText.setOnFocusChangeListener(new EditText.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) setValueOfEditText(((EditText) v).getText().toString(), v.getId());
            }
        });

        // also set onEditorActionListener
        editText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    setValueOfEditText(((EditText) v).getText().toString(), v.getId());
                }
                return false;
            }
        });
    }

    public void setValueOfEditText(String text, int viewId)
    {
        Log.d("onFocusChange", viewId + ":" + text );
        if(text.isEmpty()) {
            text = "0";
        }

        switch (viewId) {
            case R.id.etSubTotal:
                if (!mBill.updateSubTotal(new BigDecimal(text)))
                    return;
                // Check for added item
                updateItems();
                break;
            case R.id.etGST:
                if (!mBill.updateGST(new BigDecimal(text)))
                    return;
                break;
            case R.id.etGstPercent:
                if (!mBill.updateGstPercent(Integer.parseInt(text)))
                    return;
                break;
            case R.id.etSVC:
                if (!mBill.updateSVC(new BigDecimal(text)))
                    return;
                break;
            case R.id.etSvcPercent:
                if (!mBill.updateSvcPercent(Integer.parseInt(text)))
                    return;
                break;
            case R.id.etTotal:
                if (!mBill.updateTotal(new BigDecimal(text)))
                    return;
                // Check for added item
                updateItems();
                break;
            default:
                // check for editItems
                int index = viewId;
                Log.d("index edit", Integer.toString(index));

                EditText editText;
                TableRow row = (TableRow) mLayoutEditItems.getChildAt(index + 1);
                editText = (EditText) row.getChildAt(0);
                Log.d("editText 0", editText.getText().toString());
                mBill.updateItem(editText.getText().toString(), index);

                editText = (EditText) row.getChildAt(1);
                Log.d("editText 1", editText.getText().toString());
                if (!mBill.updateItem(new BigDecimal(editText.getText().toString()), index))
                    return;
                break;
        }
        // update Totals UI
        updateTotals();
    }

    // OnCreate
    private void createItems() {
        Item item;
        for (int i = 0; i < mBill.getListOfAllItems().size(); i++) {
            item = mBill.getListOfAllItems().get(i);
            addItemRow(item, i);
        }
    }

    private void addItemRow(Item item, int index) {
        EditText editTextDesc;
        EditText editTextAmt;
        ImageButton imageButtonDelete;

        TableRow row = new TableRow(this.getActivity());
        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
        row.setLayoutParams(lp);

        editTextDesc = new EditText(this.getActivity());
        editTextDesc.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        editTextDesc.setText(item.getDescription());
        editTextDesc.setId(index);
        editTextDesc.setSelectAllOnFocus(true);
        setOnFocusChangeListener(editTextDesc);
        row.addView(editTextDesc);

        editTextAmt = new EditText(this.getActivity());
        editTextAmt.setText(item.getPrice().toString());
        editTextAmt.setInputType(InputType.TYPE_CLASS_NUMBER |
                InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        editTextAmt.setId(index);
        editTextAmt.setSelectAllOnFocus(true);
        setOnFocusChangeListener(editTextAmt);
        row.addView(editTextAmt);

        imageButtonDelete = new ImageButton(this.getActivity());
        imageButtonDelete.setImageResource(R.drawable.ic_delete_black_18dp);
        imageButtonDelete.setClickable(true);
        imageButtonDelete.setId(index);
        imageButtonDelete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                deleteItem(v.getId());
            }
        });
        row.addView(imageButtonDelete);

        mLayoutEditItems.addView(row, index+1);
    }

    // check if there are any new items
    private void updateItems() {
        int numOfItems = mBill.getListOfAllItems().size();
        if (mLayoutEditItems.getChildCount() == (numOfItems + 1))
            return;
        Log.d("updateItems", Integer.toString(numOfItems));

        Item item = mBill.getListOfAllItems().get(numOfItems-1);
        addItemRow(item, numOfItems-1);
    }

    private void updateTotals() {
        mSubtotal.setText(mBill.getSubTotal().toString());
        mGST.setText(mBill.getGst().toString());
        mGstPercent.setText(Integer.toString(mBill.getGstPercent()));
        mSVC.setText(mBill.getSvc().toString());
        mSvcPercent.setText(Integer.toString(mBill.getSvcPercent()));
        mTotal.setText(mBill.getTotal().toString());
    }

    // User clicked add new item button
    public void addNewItem() {
        mBill.addItem();
        // populate new item in mEditItemLayout
        int index = mBill.getListOfAllItems().size()-1;
        Item item = mBill.getListOfAllItems().get(index);
        addItemRow(item, index);
    }

    public void deleteItem(int itemIndex) {
        Log.d ("deleteItem", Integer.toString(itemIndex));
        // delete item at location itemIndex
        mBill.deleteItem(itemIndex);
        mLayoutEditItems.removeViewAt(itemIndex + 1);

        // and update the rest
        for (int i = (itemIndex+1); i < (mLayoutEditItems.getChildCount()); i++) {
            TableRow row = (TableRow) mLayoutEditItems.getChildAt(i);
            row.getChildAt(0).setId(i-1);
            row.getChildAt(1).setId(i-1);
            row.getChildAt(2).setId(i-1);
        }

        updateTotals();
    }
}
