package com.hzp.cocorunning;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.hzp.cocorunning.IMCase.ui.IMmainActivity;
import com.hzp.cocorunning.ui.AllCard3dActivity;
import com.hzp.cocorunning.ui.FinishPackage.MissionFinishActivity;
import com.hzp.cocorunning.ui.LoginActivity;
import com.hzp.cocorunning.ui.unityActivities.UnityPlayerActivity;
import com.hzp.cocorunning.util.Constans;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import static java.lang.Thread.sleep;


public class MainActivity extends AppCompatActivity implements AMap.OnMyLocationChangeListener{
    private MainActivity self = this;
    MyLocationStyle myLocationStyle;
    public LatLng latlngA= null;//北邮学6坐标
    int latLngNumber = 0;
    //指示点的坐标
    private LatLng latlngDirection;
//    TextView text;
//    TextView tex1;
    private double distance = 10000;
    private Marker marker;
    //主地图空间
    private MapView mMapView = null;
    private AMap aMap;


    //cameraUpdate对象来更新对象状态
    CameraUpdate cameraUpdate;

      //处理距离判断的线程 的what参数
    public static final int JUDGE_DISTANCE = 1;
    //线程使用的handler  创建一个线程来进行实时距离判断
    @SuppressLint("HandlerLeak")
    private android.os.Handler handler = new android.os.Handler(){
        @SuppressLint("SetTextI18n")
        public void handleMessage(android.os.Message msg){
            switch (msg.what){
                case JUDGE_DISTANCE:
                    Intent intent = new Intent(self,MissionFinishActivity.class);

                    Random random=new Random();// 定义随机类
                    latLngNumber=random.nextInt(6);

                    latlngA = Constans.latLngList.get(latLngNumber);
//                    tex1.setText(latLngNumber+"  当前目的地点坐标"+latlngA);
                    startActivity(intent);
            }
        }
    };

    public MainActivity() {
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        //设定初始的坐标为北邮学6
        latlngA = Constans.latLngList.get(latLngNumber);

//        text = findViewById(R.id.info_text);
//        tex1 = findViewById(R.id.info_text1);
        //获取地图控件的引用
        mMapView = findViewById(R.id.map);
        //创建地图
        mMapView.onCreate(savedInstanceState);//此方法须覆写，虚拟机需要在很多情况下保存地图绘制的当前状态。
        //初始化地图控制器对象
        aMap = mMapView.getMap();


        //定位小蓝点的显示
        myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。

        //半透明的白
        myLocationStyle.strokeColor(Color.argb(2,255,255,255));//设置定位蓝点精度圆圈的边框颜色的方法。
        myLocationStyle.radiusFillColor(Color.argb(100,255,255,255));//设置定位蓝点精度圆圈的填充颜色的方法。
        myLocationStyle.strokeWidth((float) 2.0);
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory
                .fromResource(R.drawable.br_up));// 设置小蓝点的图标
        myLocationStyle.interval(1500); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_MAP_ROTATE);
        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        aMap.getUiSettings().setMyLocationButtonEnabled(false);//设置默认定位按钮是否显示，非必需设置。
        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。

        //位置改变监听
        aMap.setOnMyLocationChangeListener(this);
        //设置缩放
        aMap.setMinZoomLevel(19);
        aMap.setMaxZoomLevel(19);
        //设置地图样式
        setMapCustomStyleFile(this,aMap);
        aMap.setMapCustomEnable(true);
        //设置地图的倾斜角度
        cameraUpdate = CameraUpdateFactory.changeTilt(70);
        aMap.animateCamera(cameraUpdate);


        //设置一些地图的参数
        UiSettings mUiSettings = aMap.getUiSettings();
        mUiSettings.setTiltGesturesEnabled(false);// 设置地图是否可以倾斜
        mUiSettings.setScaleControlsEnabled(true);// 设置地图默认的比例尺是否显示
        mUiSettings.setZoomControlsEnabled(false);//去掉右下角缩放键
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (judgeTheDistance(distance)) {
                        Message message = new Message();
                        message.what = JUDGE_DISTANCE;
                        handler.sendMessage(message);
                        try {
                            sleep(2000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();





        TextView testAR = findViewById(R.id.test_AR);
        testAR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(self, UnityPlayerActivity.class));
            }
        });
        TextView test3d = findViewById(R.id.test_3d);
        test3d.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(self, AllCard3dActivity.class));
            }
        });
        TextView textIM=findViewById(R.id.test_IM);
        textIM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(self, LoginActivity.class));
            }
        });


    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        //活动销毁时销毁地图
        mMapView.onDestroy();
        //停止定位，销毁定位客户端
    }
    @Override
    protected void onResume(){
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onMyLocationChange(Location location) {
        LatLng latlngB = new LatLng(location.getLatitude(),location.getLongitude());
        distance = AMapUtils.calculateLineDistance(latlngA, latlngB);

//        text.setText("你的坐标为："+latlngB+"\n距离目标点:"+distance+"m\n");
//        在你的周围绘制一个圆圈
//        if(circle!=null){
//            circle.remove();
//        }
//        double radius = 26;
//        circle = aMap.addCircle(new CircleOptions().
//                    center(latlngB).
//                    radius(radius).
//                    //白色的圈
//                    fillColor(Color.argb(0, 1, 1, 1)).
//                    strokeColor(Color.argb(255, 255, 255, 255)).
//                    strokeWidth(5));




        //显示指明方向的光
        double lat = latlngA.latitude-latlngB.latitude;
        double lng = latlngA.longitude-latlngB.longitude;
        if(distance>250){
            //计算出指示方向的点的经纬度
            latlngDirection = new LatLng(((800/distance)*lat+latlngB.latitude),((800/distance)*lng+latlngB.longitude));
            //在地图上画出指示方向的光
            setDirection(R.drawable.far_light);

        }else{
            latlngDirection = new LatLng(latlngA.latitude,latlngA.longitude);

            //latlngDirection = new LatLng(latlngB.latitude,latlngB.longitude);
            setDirection(R.drawable.far_light_middle);
        }

        //使用一条线来代替点指明方向

//        List<LatLng> latLngs = new ArrayList<LatLng>();
//        latLngs.add(latlngA);
//        latLngs.add(latlngB);
//        aMap.addPolyline(new PolylineOptions().
//                addAll(latLngs).width(10).color(Color.argb(255, 255, 255, 255)));

        //使用夹角指明方向
        //double myBearingToNorth = location.getBearing();


        //        //使用高德地图的poi搜索功能，对周边的标志性建筑物进行检索
//        int currentPage= 0;
//        keyword = "美食";
//        query = new PoiSearch.Query(keyword, "", "");
//        //keyWord表示搜索字符串，
//        //第二个参数表示POI搜索类型，二者选填其一，选用POI搜索类型时建议填写类型代码，码表可以参考下方（而非文字）
//        //cityCode表示POI搜索区域，可以是城市编码也可以是城市名称，也可以传空字符串，空字符串代表全国在全国范围内进行搜索
//        query.setPageSize(20);// 设置每页最多返回多少条poiitem
//        query.setPageNum(currentPage);//设置查询页码
//
//        if(latlngB!=null){
//
//            poiSearch = new PoiSearch(self, query);
//            poiSearch.setOnPoiSearchListener(self);
//            LatLonPoint lp = new LatLonPoint(latlngB.latitude, latlngB.longitude);
//            poiSearch.setBound(new PoiSearch.SearchBound(lp, 5000, true));//
//            // 设置搜索区域为以lp点为圆心，其周围5000米范围
//            poiSearch.searchPOIAsyn();// 异步搜索
//        }
    }

    //获取自定义地图的文件位置，并进行相关设置
    private void setMapCustomStyleFile(Context context,AMap aMap) {
        String styleName = "style.data";
        FileOutputStream outputStream = null;
        InputStream inputStream = null;
        String filePath = null;
        try {
            inputStream = context.getAssets().open(styleName);
            byte[] b = new byte[inputStream.available()];
            inputStream.read(b);

            filePath = context.getFilesDir().getAbsolutePath();
            File file = new File(filePath + "/" + styleName);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            outputStream = new FileOutputStream(file);
            outputStream.write(b);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();

                if (outputStream != null)
                    outputStream.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        aMap.setCustomMapStylePath(filePath + "/" + styleName);

    }

    //判断是否已经到达目的地
    public boolean judgeTheDistance(double distance) {
        return !(distance > 200) && distance >= 0;
    }
    //判断是否已接近目的地
    //public boolean isClosetoDestination(double distance){return !(distance>550) && distance>=0;  }


    //显示一个maker，来标识方位
    public void setDirection(int x){
        if(marker!=null){
            marker.destroy();
        }
        MarkerOptions markerOption = new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(x))
                .position(latlngDirection)
                .draggable(true);
        marker = aMap.addMarker(markerOption);
    }

}
