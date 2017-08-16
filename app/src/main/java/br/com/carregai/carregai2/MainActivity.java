package br.com.carregai.carregai2;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.util.Calendar;
import java.util.GregorianCalendar;

import br.com.carregai.carregai2.adapter.SectionPageAdapter;
import br.com.carregai.carregai2.model.User;
import br.com.carregai.carregai2.service.UpdatingService;
import br.com.carregai.carregai2.utils.DrawerUtils;
import br.com.carregai.carregai2.utils.Utility;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements Drawer.OnDrawerItemClickListener{

    public static final String SERVICES_FRAGMENT = "Serviços";
    public static final String ORDERS_FRAGMENT = "Recargas";
    public static final int TOTAL_FRAGMENTS = 2;

    @BindView(R.id.main_page_toolbar)
    Toolbar mToolbar;

    @BindView(R.id.main_view_pager)
    ViewPager mViewPager;

    @BindView(R.id.main_tab)
    TabLayout mTabLayout;

    private SectionPageAdapter mAdapter;

    private DatabaseReference mUsersDatabase;

    private Drawer mDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users");

        Log.i("ID USER", user.getUid());

        if(user != null){
            DatabaseReference child = userRef.child(user.getUid());

            child.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    User usermodel = dataSnapshot.getValue(User.class);

                    Log.i("USER: ", usermodel.toString());

                    FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();

                    Drawer drawer = new DrawerUtils()
                            .setUpCustomerDrawer(MainActivity.this,
                                    usermodel.getName(),
                                    usermodel.getEmail(),
                                    mUser.getPhotoUrl(),
                                    mToolbar).build();

                    drawer.setOnDrawerItemClickListener(MainActivity.this);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        mAdapter = new SectionPageAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);

        mTabLayout.setupWithViewPager(mViewPager);
    }


    @Override
    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
        DrawerUtils.onUserClickListener(position, this, this);
        Utility.makeText(this, String.valueOf(position));
        return false;
    }

    public void trigger(){

        Calendar calendar = (GregorianCalendar) Calendar.getInstance();

        Intent myIntent = new Intent(MainActivity.this, UpdatingService.class);

        PendingIntent pendingIntent = pendingIntent = PendingIntent.getService(MainActivity.this, 0,
                myIntent, 0);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),
                30 * 1000, pendingIntent);
    }
}
