package com.beta.zem.patientcareapp.Activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.beta.zem.patientcareapp.ConfigurationModule.Helpers;
import com.beta.zem.patientcareapp.Controllers.DbHelper;
import com.beta.zem.patientcareapp.Controllers.OrderPreferenceController;
import com.beta.zem.patientcareapp.ImageGallery.CirclePageIndicator;
import com.beta.zem.patientcareapp.Controllers.PatientController;
import com.beta.zem.patientcareapp.Model.OrderModel;
import com.beta.zem.patientcareapp.Model.Product;
import com.beta.zem.patientcareapp.Interface.ErrorListener;
import com.beta.zem.patientcareapp.Interface.RespondListener;
import com.beta.zem.patientcareapp.Network.ListOfPatientsRequest;
import com.beta.zem.patientcareapp.Network.PostRequest;
import com.beta.zem.patientcareapp.ShowPrescriptionDialog;
import com.beta.zem.patientcareapp.SidebarModule.SidebarActivity;
import com.beta.zem.patientcareapp.adapter.CircleFragmentAdapter;
import com.beta.zem.patientcareapp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SelectedProductActivity extends AppCompatActivity implements View.OnClickListener {
    int get_productID = 0, temp_qty = 1, qtyPerPacking = 1, available_qty = 0;
    public static int is_resumed = 0;

    LinearLayout add_to_cart, out_of_stock, edit_layout;
    RelativeLayout root;

    ImageButton minus_qty, add_qty;
    Toolbar myToolBar;
    ViewPager pager;
    CirclePageIndicator indicator;
    TextView prod_name, prod_generic, prod_unit, prod_price, qty_cart, prod_description;

    Handler handler;
    DbHelper dbhelper;
    Product product;
    Helpers helpers;
    PatientController ptc;
    OrderModel order_model;
    OrderPreferenceController opc;
    CircleFragmentAdapter adapter;

    public static AppCompatDialog pDialog;
    AlertDialog.Builder builder;

    public HashMap<String, String> map = new HashMap<>();
    private TextView number_of_notif;

    int basket_count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selected_product_layout);

        handler = new Handler();
        dbhelper = new DbHelper(this);
        ptc = new PatientController(this);
        opc = new OrderPreferenceController(this);
        helpers = new Helpers();

        order_model = opc.getOrderPreference();

        prod_name = (TextView) findViewById(R.id.prod_name);
        prod_generic = (TextView) findViewById(R.id.prod_generic);
        prod_unit = (TextView) findViewById(R.id.prod_unit);
        prod_price = (TextView) findViewById(R.id.prod_price);
        qty_cart = (TextView) findViewById(R.id.qty_cart);
        minus_qty = (ImageButton) findViewById(R.id.minus_qty);
        add_qty = (ImageButton) findViewById(R.id.add_qty);
        add_to_cart = (LinearLayout) findViewById(R.id.add_to_cart);
        prod_description = (TextView) findViewById(R.id.prod_description);
        pager = (ViewPager) findViewById(R.id.pager);
        indicator = (CirclePageIndicator) findViewById(R.id.indicator);
        out_of_stock = (LinearLayout) findViewById(R.id.out_of_stock);
        edit_layout = (LinearLayout) findViewById(R.id.edit_layout);
        root = (RelativeLayout) findViewById(R.id.root);

        minus_qty.setOnClickListener(this);
        add_qty.setOnClickListener(this);
        add_to_cart.setOnClickListener(this);

        myToolBar = (Toolbar) findViewById(R.id.myToolBar);
        setSupportActionBar(myToolBar);

        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        get_productID = intent.getIntExtra("productID", 0);

        showBeautifulDialog();
        product = getProductWithImage(get_productID);
        qty_cart.setText(String.valueOf(temp_qty));
    }

    void showBeautifulDialog() {
        builder = new AlertDialog.Builder(SelectedProductActivity.this);
        builder.setView(R.layout.progress_stuffing);
        builder.setCancelable(false);
        pDialog = builder.create();
        pDialog.show();
    }

    void letDialogSleep() {
        pDialog.dismiss();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.go_to_cart_menu, menu);

        MenuItem item = menu.findItem(R.id.go_to_cart);
        MenuItemCompat.setActionView(item, R.layout.count_badge_layout);
        RelativeLayout badgeLayout = (RelativeLayout) MenuItemCompat.getActionView(item);

        number_of_notif = (TextView) badgeLayout.findViewById(R.id.number_of_notif);
        ImageButton go_to_cart = (ImageButton) badgeLayout.findViewById(R.id.go_to_cart);
        go_to_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SelectedProductActivity.this, ShoppingCartActivity.class));
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.minus_qty:
                temp_qty -= 1;
                minus_qty.setImageResource(R.drawable.ic_minus_dead);

                if (temp_qty < 1) {
                    temp_qty = 1;
                    qty_cart.setText(String.valueOf(temp_qty));
                } else
                    qty_cart.setText(String.valueOf(temp_qty));

                prod_unit.setText("1 " + product.getPacking() + " x " + temp_qty + "(" + (temp_qty * product.getQtyPerPacking()) + " " + product.getUnit() + ")");
                prod_price.setText("Php " + (temp_qty * product.getPrice()));

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        minus_qty.setImageResource(R.drawable.ic_minus);
                    }
                }, 100);
                break;

            case R.id.add_qty:
                if (temp_qty < available_qty) {
                    temp_qty += 1;
                    add_qty.setImageResource(R.drawable.ic_plus_dead);
                    prod_price.setText("Php " + (temp_qty * product.getPrice()));
                    qty_cart.setText("" + temp_qty);
                    prod_unit.setText("1 " + product.getPacking() + " x " + temp_qty + "(" + (temp_qty * product.getQtyPerPacking()) + " " + product.getUnit() + ")");

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            add_qty.setImageResource(R.drawable.ic_plus);
                        }
                    }, 100);
                } else {
                    Toast.makeText(getBaseContext(), available_qty + " is the maximum available stock", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.add_to_cart:
                final HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("table", "baskets");
                hashMap.put("product_id", String.valueOf(get_productID));
                hashMap.put("patient_id", String.valueOf(SidebarActivity.getUserID()));
                hashMap.put("request", "crud");

                showBeautifulDialog();

                String url_raw = "get_basket_items&patient_id=" + SidebarActivity.getUserID() + "&table=baskets";
                ListOfPatientsRequest.getJSONobj(url_raw, "baskets", new RespondListener<JSONObject>() {
                    @Override
                    public void getResult(JSONObject response) {
                        try {
                            int success = response.getInt("success");
                            int check = 0;
                            final HashMap<String, String> old_basket = new HashMap<>();

                            if (success == 1) {
                                if (response.getBoolean("has_contents")) {

                                    JSONArray json_mysql = response.getJSONArray("baskets");

                                    for (int x = 0; x < json_mysql.length(); x++) {
                                        JSONObject obj = json_mysql.getJSONObject(x);
                                        basket_count += 1;

                                        Log.d("object", obj + "");

                                        if (get_productID == obj.getInt("product_id")) {
                                            check += 1;
                                            old_basket.put("basket_id", obj.getString("id"));
                                            old_basket.put("product_id", obj.getString("product_id"));
                                            old_basket.put("old_qty", obj.getString("quantity"));
                                        }
                                    }
                                }
                            }

                            if (check >= 1) { //IF EXISTING SA CART
                                int new_basket_qty = Integer.parseInt(old_basket.get("old_qty")) + temp_qty;

                                hashMap.put("quantity", String.valueOf(new_basket_qty));
                                hashMap.put("action", "update");
                                hashMap.put("id", String.valueOf(old_basket.get("basket_id")));

                                PostRequest.send(getBaseContext(), hashMap, new RespondListener<JSONObject>() {
                                    @Override
                                    public void getResult(JSONObject response) {
                                        try {
                                            int success = response.getInt("success");

                                            if (success == 1)
                                                Snackbar.make(root, "Your cart has been updated", Snackbar.LENGTH_SHORT).show();
                                        } catch (JSONException e) {
                                            Snackbar.make(root, e + "", Snackbar.LENGTH_SHORT).show();
                                        }
                                        letDialogSleep();
                                    }
                                }, new ErrorListener<VolleyError>() {
                                    public void getError(VolleyError error) {
                                        letDialogSleep();
                                        Snackbar.make(root, "Please check your Internet connection", Snackbar.LENGTH_SHORT).show();
                                    }
                                });
                            } else { //IF NEW ITEM
                                hashMap.put("quantity", String.valueOf(temp_qty));
                                hashMap.put("action", "insert");

                                if (product.getPrescriptionRequired() == 1) { //IF PRODUCT REQUIRES PRESCRIPTION
                                    GridView gridView;
                                    final Dialog builder;
                                    HashMap<GridView, Dialog> map = helpers.showPrescriptionDialog(SelectedProductActivity.this);

                                    if (map.size() > 0) { //IF NAA NAY UPLOADED NGA IMAGES/S
                                        Map.Entry<GridView, Dialog> entry = map.entrySet().iterator().next();
                                        gridView = entry.getKey();
                                        builder = entry.getValue();

                                        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                showBeautifulDialog();
                                                int prescriptionId = (int) id;
                                                hashMap.put("prescription_id", prescriptionId + "");
                                                hashMap.put("is_approved", "0");

                                                PostRequest.send(SelectedProductActivity.this, hashMap, new RespondListener<JSONObject>() {
                                                    @Override
                                                    public void getResult(JSONObject response) {
                                                        try {
                                                            int success = response.getInt("success");

                                                            if (success == 1) {
                                                                hashMap.put("server_id", String.valueOf(response.getInt("last_inserted_id")));
                                                                Snackbar.make(root, "New item has been added to your cart", Snackbar.LENGTH_SHORT).show();

                                                                number_of_notif.setVisibility(View.VISIBLE);
                                                                number_of_notif.setText(String.valueOf(basket_count + 1));
                                                            }
                                                        } catch (JSONException e) {
                                                            Log.d("selected_prod6", e + "");
                                                            Snackbar.make(root, "Server error occurred", Snackbar.LENGTH_SHORT).show();
                                                        }
                                                        letDialogSleep();
                                                    }
                                                }, new ErrorListener<VolleyError>() {
                                                    public void getError(VolleyError error) {
                                                        letDialogSleep();
                                                        Log.d("selected_prod5", error + "");
                                                        Snackbar.make(root, "Network error", Snackbar.LENGTH_SHORT).show();
                                                    }
                                                });
                                                builder.dismiss();
                                            }
                                        });
                                        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                            @Override
                                            public void onCancel(DialogInterface dialog) {
                                                dialog.dismiss();
                                            }
                                        });
                                    } else { //IF WALA PA NAKAUPLOAD BSAG ISA
                                        AlertDialog.Builder confirmationDialog = new AlertDialog.Builder(SelectedProductActivity.this);
                                        confirmationDialog.setTitle("Attention!");
                                        confirmationDialog.setMessage("This product requires you to upload a prescription, do you wish to continue ?");
                                        confirmationDialog.setNegativeButton("No", null);
                                        confirmationDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                SelectedProductActivity.this.map = hashMap;
                                                startActivity(new Intent(SelectedProductActivity.this, ShowPrescriptionDialog.class));
                                            }
                                        });
                                        confirmationDialog.show();
                                    }
                                } else { //IF NO PRESCRIPTION IS REQUIRED
                                    hashMap.put("prescription_id", "0");
                                    hashMap.put("is_approved", "1");
                                    letDialogSleep();

                                    PostRequest.send(getBaseContext(), hashMap, new RespondListener<JSONObject>() {
                                        @Override
                                        public void getResult(JSONObject response) {
                                            try {
                                                Snackbar.make(root, "New item has been added to your cart", Snackbar.LENGTH_SHORT).show();

                                                number_of_notif.setVisibility(View.VISIBLE);
                                                number_of_notif.setText(String.valueOf(basket_count + 1));
                                            } catch (Exception e) {
                                                Log.d("selected_prod4", e + "");
                                                Snackbar.make(root, "Server error occurred", Snackbar.LENGTH_SHORT).show();
                                            }
                                            letDialogSleep();
                                        }
                                    }, new ErrorListener<VolleyError>() {
                                        public void getError(VolleyError error) {
                                            letDialogSleep();
                                            Log.d("selected_prod3", error + "");
                                            Snackbar.make(root, "Network error", Snackbar.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        } catch (Exception e) {
                            Log.d("selected_prod2", e + "");
                            Snackbar.make(root, "Server error occurred", Snackbar.LENGTH_SHORT).show();
                        }
                        letDialogSleep();
                    }
                }, new ErrorListener<VolleyError>() {
                    @Override
                    public void getError(VolleyError e) {
                        letDialogSleep();
                        Log.d("selected_prod1", e + "");
                        Snackbar.make(root, "Network error", Snackbar.LENGTH_SHORT).show();
                    }
                });

                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        getAllBasketItems();
        CircleFragmentAdapter.CONTENT.clear();
        ProductsActivity.is_finish = 0;

        if (is_resumed != 0) {
            showBeautifulDialog();

            int prescriptionId = is_resumed;
            final HashMap<String, String> hashMap = map;
            hashMap.put("prescription_id", prescriptionId + "");
            hashMap.put("is_approved", "0");

            PostRequest.send(this, hashMap, new RespondListener<JSONObject>() {
                @Override
                public void getResult(JSONObject response) {
                    try {
                        int success = response.getInt("success");

                        if (success == 1) {
                            Snackbar.make(root, "New item has been added to your cart", Snackbar.LENGTH_SHORT).show();

                            number_of_notif.setVisibility(View.VISIBLE);
                            number_of_notif.setText(String.valueOf(basket_count + 1));
                        }
                    } catch (Exception e) {
                        Snackbar.make(root, e + "", Snackbar.LENGTH_SHORT).show();
                    }
                    letDialogSleep();
                }
            }, new ErrorListener<VolleyError>() {
                public void getError(VolleyError error) {
                    letDialogSleep();
                    Snackbar.make(root, "Please check your Internet connection", Snackbar.LENGTH_SHORT).show();
                }
            });
        }

        is_resumed = 0;
    }

    public Product getProductWithImage(int product_id) {
        final ArrayList<String> filenames = new ArrayList<>();
        final Product prod = new Product();

        ListOfPatientsRequest.getJSONobj("get_selected_product_with_image&product_id=" + product_id + "&branch_id=" + order_model.getBranch_id(), "products", new RespondListener<JSONObject>() {
            @Override
            public void getResult(JSONObject response) {
                try {
                    int success = response.getInt("success");

                    if (success == 1) {
                        if (response.getBoolean("has_contents")) {
                        JSONArray json_mysql = response.getJSONArray("products");

                        for (int x = 0; x < json_mysql.length(); x++) {
                            JSONObject obj = json_mysql.getJSONObject(x);

                            if (!obj.getString("filename").equals("")) {
                                filenames.add(obj.getString("filename"));
                            } else
                                filenames.add("default");
                        }

                        JSONObject obj = json_mysql.getJSONObject(0);
                        prod.setId(obj.getInt("id"));
                        prod.setName(obj.getString("name"));
                        prod.setGenericName(obj.getString("generic_name"));
                        prod.setDescription(obj.getString("description"));
                        prod.setPrescriptionRequired(obj.getInt("prescription_required"));
                        prod.setPrice(obj.getDouble("price"));
                        prod.setUnit(obj.getString("unit"));
                        prod.setPacking(obj.getString("packing"));
                        prod.setQtyPerPacking(obj.getInt("qty_per_packing"));
                        prod.setSku(obj.getString("sku"));
                        prod.setAvailableQuantity(obj.getInt("available_quantity"));
                    }
                }
                } catch (Exception e) {
                    Snackbar.make(root, e + "", Snackbar.LENGTH_SHORT).show();
                }

                available_qty = prod.getAvailableQuantity();

                if (available_qty == 0) {
                    out_of_stock.setVisibility(View.VISIBLE);
                    add_to_cart.setVisibility(View.GONE);
                } else
                    edit_layout.setVisibility(View.VISIBLE);

                prod_name.setText(prod.getName());
                prod_generic.setText(prod.getGenericName());
                prod_description.setText(prod.getDescription());
                prod_price.setText("Php " + prod.getPrice());
                qtyPerPacking = prod.getQtyPerPacking();
                prod_unit.setText("1 " + prod.getPacking() + " x " + temp_qty + "(" + prod.getQtyPerPacking() + " " + prod.getUnit() + ")");

                assert getSupportActionBar() != null;
                getSupportActionBar().setTitle(prod.getName());

                CircleFragmentAdapter.CONTENT.addAll(filenames);
                adapter = new CircleFragmentAdapter(getSupportFragmentManager());
                pager.setAdapter(adapter);
                indicator.setViewPager(pager);

                letDialogSleep();
            }
        }, new ErrorListener<VolleyError>() {
            @Override
            public void getError(VolleyError e) {
                letDialogSleep();
                Snackbar.make(root, "Network error", Snackbar.LENGTH_SHORT).show();
            }
        });

        return prod;
    }

    private void getAllBasketItems() {
        String url_raw = "get_basket_items&patient_id=" + SidebarActivity.getUserID() + "&table=baskets";

        ListOfPatientsRequest.getJSONobj(url_raw, "baskets", new RespondListener<JSONObject>() {
            @Override
            public void getResult(JSONObject response) {
                try {
                    int success = response.getInt("success");
                    int count = 0;
                    if (success == 1) {
                        if (response.getBoolean("has_contents")) {

                            JSONArray json_mysql = response.getJSONArray("baskets");

                            for (int x = 0; x < json_mysql.length(); x++)
                                count++;

                            if (count > 0) {
                                number_of_notif.setVisibility(View.VISIBLE);
                                number_of_notif.setText(String.valueOf(count));
                            }
                        }
                    } else
                        number_of_notif.setVisibility(View.GONE);
                } catch (Exception e) {
                    Log.d("SidebarAct2", e + "");
                    Snackbar.make(root, "Error occurred", Snackbar.LENGTH_SHORT).show();
                }
            }
        }, new ErrorListener<VolleyError>() {
            @Override
            public void getError(VolleyError e) {
                Snackbar.make(root, "Network error", Snackbar.LENGTH_SHORT).show();
            }
        });
    }
}
