package com.example.lyzy.dragon;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapOnTouchListener;
import com.esri.android.map.MapView;
import com.esri.android.map.RasterLayer;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.android.runtime.ArcGISRuntime;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.MultiPath;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.Graphic;
import com.esri.core.raster.FileRasterSource;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Main extends Activity
{
    RasterLayer rasterLayer;
    String rasterPath = Environment.getExternalStorageDirectory().getPath() + "/test/ligong.tif";
    private static final String TAG = null;
    FileRasterSource rasterSource = null;
    MapView mapView = null;
    GraphicsLayer graphicsLayer;
    GraphicsLayer graphicsLayeredit;
    Point startPoint = null;
    //    存储绘制 线 面 轨迹数据
    MultiPath multiPath;
    SimpleLineSymbol simpleLineSymbol;
    MyTouchListener listener1 = null;
    //    Polyline poly;
    Polygon poly;
    int uid;
    Button buttonmian;
    boolean start = false;
    int addgraphicidg;
    Geometry.Type type = null;
    Polyline polyline;
    int idg;
    int idl;
    String fid;
    boolean startcx;
    boolean contains;
    boolean crosses;
    double distance;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ArcGISRuntime.setClientId("1eFHW78avlnRUPHm");
        buttonmian = (Button) findViewById(R.id.button);

        mapView = (MapView) findViewById(R.id.map);
        try
        {
            rasterSource = new FileRasterSource(rasterPath);
        } catch (IllegalArgumentException ie)
        {
            Log.d(TAG, "null or empty path");
        } catch (FileNotFoundException fe)
        {
            Log.d(TAG, "raster file doesn't exist");
        } catch (RuntimeException re)
        {
            Log.d(TAG, "raster file can't be opened");
        }
        rasterLayer = new RasterLayer(rasterSource);
        mapView.addLayer(rasterLayer);
        graphicsLayer = new GraphicsLayer();
        mapView.addLayer(graphicsLayer);
        graphicsLayeredit = new GraphicsLayer();

        mapView.addLayer(graphicsLayeredit);

        listener1 = new MyTouchListener(this, mapView);
        mapView.setOnTouchListener(listener1);

        mapView.setOnSingleTapListener(new OnSingleTapListener()
        {
            private static final long serialVersionUID = 1L;

            public void onSingleTap(float x, float y)
            {
                if (startcx)
                {
                    // gets the first 1000 features at the clicked point on the map, within 10 pixels
                    int[] ids = graphicsLayeredit.getGraphicIDs(x, y, 100, 1);
                    graphicsLayeredit.setSelectionColorWidth(5);
                    graphicsLayeredit.setSelectionColor(2);
                    graphicsLayeredit.setSelectedGraphics(ids, true);

                    if (ids.length > 0)
                    {
                        Graphic graphicselected = graphicsLayeredit.getGraphic(ids[0]);


                        fid = String.valueOf(graphicselected.getId());

                        Toast.makeText(getApplicationContext(),
                                "FID=" + fid, Toast.LENGTH_SHORT).show();

                    } else
                    { //提示无选中要素
                        Log.i("", "无选中要素");
                    }
                }
            }
        });


    }

    public class MyTouchListener extends MapOnTouchListener
    {

        public MyTouchListener(Context context, MapView view)
        {
            //        引用父类方法
            super(context, view);
        }

        //    挡在屏幕上滑动时，将滑动生成的点逐步加入poly变量中；
        @Override
        public boolean onDragPointerMove(MotionEvent from, MotionEvent to)
        {
            if (start)
            {
                if (type == Geometry.Type.POLYGON)
                {
                    Point currentPoint = mapView.toMapPoint(to.getX(), to.getY());
                    if (startPoint == null)
                    {//判断是否已经存在第一个点
                        multiPath = new Polygon();
                        startPoint = mapView.toMapPoint(from.getX(), from.getY());
                        multiPath.startPath((float) startPoint.getX(), (float) startPoint.getY());
                        uid = graphicsLayer.addGraphic(new Graphic(multiPath, new SimpleLineSymbol(Color.BLUE, 2)));
                    }
                    multiPath.lineTo((float) currentPoint.getX(), (float) currentPoint.getY());
                    //增加线点
                    graphicsLayer.updateGraphic(uid, multiPath);
                    //更新数据显示
                }
                if (type == Geometry.Type.POLYLINE)
                {
                    Point currentPoint = mapView.toMapPoint(to.getX(), to.getY());
                    if (startPoint == null)
                    {//判断是否已经存在第一个点
                        multiPath = new Polyline();
                        startPoint = mapView.toMapPoint(from.getX(), from.getY());
                        multiPath.startPath((float) startPoint.getX(), (float) startPoint.getY());
                        uid = graphicsLayer.addGraphic(new Graphic(multiPath, new SimpleLineSymbol(Color.BLUE, 2)));
                    }
                    multiPath.lineTo((float) currentPoint.getX(), (float) currentPoint.getY());
                    //增加线点
                    graphicsLayer.updateGraphic(uid, multiPath);

                }
            }
            return true;


        }

    }

    public void bihelistener(View view)
    {
        if (type == Geometry.Type.POLYGON)
        {
            SimpleFillSymbol simpleFillSymbol = new SimpleFillSymbol(Color.RED, SimpleFillSymbol.STYLE.NULL);
            graphicsLayer.removeAll();
            idg = graphicsLayeredit.addGraphic(new Graphic(multiPath, simpleFillSymbol));
        }
        if (type == Geometry.Type.POLYLINE)
        {
            graphicsLayer.removeAll();
            idl = graphicsLayeredit.addGraphic(new Graphic(multiPath, new SimpleLineSymbol(Color.BLACK, 1)));
        }
        start = false;
        multiPath = null;
        startPoint = null;
        type = null;
    }

    public void btnhzlistener(View view)
    {
        if (!start)
        {
            start = true;
        } else
        {
            start = false;
        }
    }

    public void btnpolygon(View view)
    {
        type = Geometry.Type.POLYGON;
    }

    public void btnpolyline(View view)
    {
        type = Geometry.Type.POLYLINE;
    }

    public void btnxz(View view)
    {
        if (!startcx)
        {
            startcx = true;
        } else
        {
            startcx = false;
        }
    }

    public void btnxj(View view)
    {
        Geometry geometry1;
        Geometry geometry2;
        geometry1 = graphicsLayeredit.getGraphic(5).getGeometry();
        geometry2 = graphicsLayeredit.getGraphic(7).getGeometry();
        crosses = GeometryEngine.crosses(geometry1,
                geometry2, mapView.getSpatialReference());

        String s = String.valueOf(crosses);
        Toast.makeText(getApplicationContext(), s
                , Toast.LENGTH_SHORT).show();
        if (crosses)
        {
            Geometry geometryout = GeometryEngine.difference(geometry2, geometry1, mapView.getSpatialReference());
            distance = Math.round(geometry2.calculateLength2D()) - Math.round(geometryout.calculateLength2D());
            String d = String.valueOf(distance);
            Toast.makeText(getApplicationContext(), d
                    , Toast.LENGTH_SHORT).show();

        }
    }

    public void btnbh(View view)
    {
        Geometry geometry1;
        Geometry geometry2;
        geometry1 = graphicsLayeredit.getGraphic(5).getGeometry();
        geometry2 = graphicsLayeredit.getGraphic(7).getGeometry();
        contains = GeometryEngine.contains(geometry1,
                geometry2, mapView.getSpatialReference());

        String s = String.valueOf(contains);
        Toast.makeText(getApplicationContext(), s
                , Toast.LENGTH_SHORT).show();
        if (contains)
        {
            distance = Math.round(geometry2.calculateLength2D());
            String d = String.valueOf(distance);
            Toast.makeText(getApplicationContext(), d
                    , Toast.LENGTH_SHORT).show();
        }
    }

    public void distance(View view)
    {

        if(graphicsLayeredit.getGraphic(5).getGeometry().getType() == Geometry.Type.POLYGON  &&
                graphicsLayeredit.getGraphic(7).getGeometry().getType() == Geometry.Type.POLYLINE)
        {
            long distance;
            Geometry geometry1;
            Geometry geometry2;
            geometry1 = graphicsLayeredit.getGraphic(5).getGeometry();
            geometry2 = graphicsLayeredit.getGraphic(7).getGeometry();
            
            contains = GeometryEngine.contains(geometry1,
                    geometry2, mapView.getSpatialReference());
            if (contains)
            {
                distance = Math.round(geometry2.calculateLength2D());
                String d = String.valueOf(distance);
                Toast.makeText(getApplicationContext(), "巡护有效距离" + d + "米"
                        , Toast.LENGTH_SHORT).show();
            } else
            {
                crosses = GeometryEngine.crosses(geometry1,
                        geometry2, mapView.getSpatialReference());
                if (crosses)
                {
                    Geometry geometryout = GeometryEngine.difference(geometry2, geometry1, mapView.getSpatialReference());
                    distance = Math.round(geometry2.calculateLength2D()) - Math.round(geometryout.calculateLength2D());
                    String d = String.valueOf(distance);
                    Toast.makeText(getApplicationContext(), "巡护有效距离" + d + "米"
                            , Toast.LENGTH_SHORT).show();

                } else
                {
                    Toast.makeText(getApplicationContext(), "巡护有效距离" + "0米"
                            , Toast.LENGTH_SHORT).show();
                }

            }

        }else {
            Toast.makeText(getApplicationContext(),"error！"
                    , Toast.LENGTH_SHORT).show();
        }

    }

    public void btnhb(View view)
    {
        Geometry[] geometries;
        geometries = new Geometry[2];
        geometries[0] = graphicsLayeredit.getGraphic(5).getGeometry();
        geometries[1] = graphicsLayeredit.getGraphic(7).getGeometry();
        Geometry geometryu;
        int[] id = graphicsLayeredit.getSelectionIDs();
        geometryu = GeometryEngine.union(geometries, mapView.getSpatialReference());
        Graphic graphic = new Graphic(geometryu,new SimpleLineSymbol(Color.BLUE,2));
        graphicsLayeredit.addGraphic(graphic);

    }

    public void btnfg(View view)
    {
        Geometry geometry1;
        Geometry geometry2;
        Geometry geometryfg;
        geometry1 = graphicsLayeredit.getGraphic(5).getGeometry();
        geometry2 = graphicsLayeredit.getGraphic(7).getGeometry();

//        geometryfg = GeometryEngine.intersect(geometry1, geometry2, mapView.getSpatialReference());
        geometryfg = GeometryEngine.difference(geometry1, geometry2, mapView.getSpatialReference());
        Graphic graphic = new Graphic(geometryfg,new SimpleLineSymbol(Color.BLUE,2));
        graphicsLayeredit.addGraphic(graphic);



    }

    public void btnjp(View view)
    {

    }
    public void btnjc(View view)
    {
        Geometry geometry1;
        Geometry geometry2;
        Geometry geometryfg;
        geometry1 = graphicsLayeredit.getGraphic(5).getGeometry();
        geometry2 = graphicsLayeredit.getGraphic(7).getGeometry();

        geometryfg = GeometryEngine.intersect(geometry1, geometry2, mapView.getSpatialReference());
        Graphic graphic = new Graphic(geometryfg,new SimpleLineSymbol(Color.BLUE,2));
        graphicsLayeredit.addGraphic(graphic);


    }


}





