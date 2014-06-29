package com.barobot.activity;

import java.util.List;

import com.barobot.BarobotMain;
import com.barobot.R;
import com.barobot.gui.dataobjects.Engine;
import com.barobot.gui.dataobjects.Liquid_t;
import com.barobot.gui.dataobjects.Product;
import com.barobot.gui.dataobjects.Type;
import com.barobot.gui.utils.CapacityProductWrapper;
import com.barobot.gui.utils.LangTool;

import android.os.Bundle;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ProductActivity extends BarobotMain {
		
	public static String SLOT_NUMBER = "slotNumber";
	
	private int mSlotNumber;
	private Type mCurrentType;
	private Liquid_t mCurrentLiquid;
	private Product mCurrentProduct;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_product);
		// Show the Up button in the action bar.
		//setupActionBar();
		
		Intent intent = getIntent();
		SetSlotNumber(intent.getIntExtra(SLOT_NUMBER, 0));
		ButtonEnabled(false, R.id.product_liquid_new_button);
		ButtonEnabled(false, R.id.product_capacity_new_button);
		
		FillTypesList();
	}
	
	private void SetSlotNumber(int position)
	{
		mSlotNumber = position;
		setTextViewText(String.valueOf(position), R.id.product_slot_number_text);
		
		if (position != 0)
		{
			Product prod = Engine.GetInstance(this).getProduct(mSlotNumber);
			if (prod != null)
			{
				SetCurrentProduct(prod);
				
				setTextViewText(new CapacityProductWrapper(mCurrentProduct).toString(), 
						R.id.product_capacity_text );
				setTextViewText(mCurrentProduct.liquid.getName(), R.id.product_liquid_text);
				setTextViewText(mCurrentProduct.liquid.type.getName(), R.id.product_type_text);
			}
			else
			{
				ClearCurrentProduct();
			}
		}
	}
	
	public void FillTypesList()
	{
		Engine engine = Engine.GetInstance(this);
		
		List<Type> types = engine.getTypes();

		
		ArrayAdapter<Type> mAdapter = new ArrayAdapter<Type>(this, R.layout.item_layout, types);
		ListView listView = (ListView) findViewById(R.id.product_type_list);
		listView.setAdapter(mAdapter);
		
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				view.setSelected(true);
				
				
				Type type = (Type) parent.getItemAtPosition(position);
				SetCurrentType(type);
				ClearCurrentLiquid();
				ClearCurrentProduct();
				
				ClearProductCapacities();
				
				setTextViewText(type.getName(), R.id.product_type_text);
				FillLiquidList();
			}
			
		});
			
	}
	
	public void FillLiquidList()
	{
		List<Liquid_t> liquids = mCurrentType.getLiquids();
		
		ArrayAdapter<Liquid_t> mAdapter = new ArrayAdapter<Liquid_t>(this, R.layout.item_layout, liquids);
		ListView listView = (ListView) findViewById(R.id.product_liquids_list);
		listView.setAdapter(mAdapter);
		
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				
				Liquid_t liquid = (Liquid_t) parent.getItemAtPosition(position); 
				SetCurrentLiquid(liquid);
				ClearCurrentProduct();
				
				setTextViewText(liquid.getName(), R.id.product_liquid_text);
				
				FillProductCapacities(mCurrentLiquid);
			}
			
		});
		
	}
	public void FillProductCapacities(Liquid_t mCurrentLiquid2)
	{
		List<Product> products = mCurrentLiquid.getProducts();
		List<CapacityProductWrapper> prods = CapacityProductWrapper.WrapList(products);

		ArrayAdapter<CapacityProductWrapper> mAdapter = new ArrayAdapter<CapacityProductWrapper>(this, R.layout.item_layout, prods);
		ListView listView = (ListView) findViewById(R.id.product_capacities_list);
		listView.setAdapter(mAdapter);
		
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				
				CapacityProductWrapper wrapper = (CapacityProductWrapper) parent.getItemAtPosition(position);
				
				setTextViewText(wrapper.toString(), R.id.product_capacity_text);
				
				SetCurrentProduct(wrapper.getProduct());
			}
			
		});
	}
	
	public void ClearProductCapacities()
	{
		ListView listView = (ListView) findViewById(R.id.product_capacities_list);
		listView.setAdapter(null);
	}
	
	private void SetCurrentProduct(Product product)
	{
		mCurrentProduct = product;
		ButtonEnabled(true,R.id.product_button_ok);
	}
	
	private void ClearCurrentProduct()
	{
		mCurrentProduct = null;
		ButtonEnabled(false, R.id.product_button_ok);
	}

	private void SetCurrentLiquid(Liquid_t liquid)
	{
		mCurrentLiquid = liquid;
		ButtonEnabled(true, R.id.product_capacity_new_button);
	}
	
	private void ClearCurrentLiquid()
	{
		mCurrentLiquid = null;
		ButtonEnabled(false, R.id.product_capacity_new_button);
	}
	
	private void SetCurrentType(Type type)
	{
		mCurrentType = type;
		ButtonEnabled(true, R.id.product_liquid_new_button);
	}
	
	
	
	
	public void onAddNewTypeButtonClick (View view)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

	    LayoutInflater inflater = getLayoutInflater();
	    final View dialogView = inflater.inflate(R.layout.dialog_add_type, null); 

	    builder.setView(dialogView)
			.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			})
			.setPositiveButton("Add", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Type type = new Type();
					TextView tView = (TextView) dialogView.findViewById(R.id.dialog_type_name);
					type.name = tView.getText().toString();
					type.insert();
					LangTool.InsertTranslation(type.id, "type", tView.getText().toString());
					type.invalidateData();
					FillTypesList();
				}
			});
		AlertDialog ad = builder.create();
		ad.show();
	}
	
	public void onAddNewLiquidButtonClicked (View view)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

	    LayoutInflater inflater = getLayoutInflater();
	    final View dialogView = inflater.inflate(R.layout.dialog_add_liquid, null);
	    TextView tView = (TextView) dialogView.findViewById(R.id.dialog_liquid_type);
	    tView.setText(mCurrentType.getName());
	    tView.setEnabled(false);

	    builder.setView(dialogView)
			.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			})
			.setPositiveButton("Add", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
					Liquid_t liquid = new Liquid_t();
					TextView tView = (TextView) dialogView.findViewById(R.id.dialog_liquid_name);
					liquid.name = tView.getText().toString();
					liquid.type = mCurrentType;
					liquid.insert();

					LangTool.InsertTranslation(liquid.id, "liquid", tView.getText().toString());

					Engine engine = Engine.GetInstance(ProductActivity.this);

					int[] defaultCapacity = {1500,1000,750,700,500,330,200,100};
					for( int i=0;i<defaultCapacity.length;i++){
						Product product = new Product();
						product.capacity = defaultCapacity[i];
						product.liquid = liquid;
						product.insert();
					}
					engine.invalidateData();
					mCurrentLiquid = liquid;
					FillProductCapacities(liquid);
					FillLiquidList();
				}
			});
		AlertDialog ad = builder.create();
		ad.show();
	}
	
	public void onAddNewProductButtonClicked (View view)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

	    LayoutInflater inflater = getLayoutInflater();
	    final View dialogView = inflater.inflate(R.layout.dialog_add_product, null);
	    TextView tView = (TextView) dialogView.findViewById(R.id.dialog_product_name);
	    tView.setText(mCurrentLiquid.getName());
	    tView.setEnabled(false);

	    builder.setView(dialogView)
			.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			})
			.setPositiveButton("Add", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Product product = new Product();
					TextView tView = (TextView) dialogView.findViewById(R.id.dialog_product_capacity);

					product.capacity = Integer.parseInt(tView.getText().toString());
					product.liquid = mCurrentLiquid;

					Engine engine = Engine.GetInstance(ProductActivity.this);
					product.insert();
					engine.invalidateData();

					FillProductCapacities(mCurrentLiquid);
				}
			});
		AlertDialog ad = builder.create();
		ad.show();
	}
	
	public void onEmptyButtonClicked (View view)
	{
		Engine.GetInstance(this).emptySlot(mSlotNumber);
		this.finish();
	}
	
	public void onOKButtonClicked (View view)
	{
		if (mCurrentProduct != null)
		{
			Engine.GetInstance(this).updateSlot(mSlotNumber, mCurrentProduct);
			this.finish();	
		}
	}
	public void onCancelButtonClicked (View view)
	{
		this.finish();
	}
}
