#include <jni.h>
#include <vector>

#include <iostream>
#include <cmath>
#include <queue>
#include <map>
#include <string>

using namespace std;
// float for actual distance from start to goal and vector for storing path

#define ppi pair<pair<int,float>,vector<pair<float,float>>>

// here we assume it goes 2 km with 1% baterry
#define id 2
#define float double


// here we use haversince distance for calculatiing staight line distance
float straight_distance(float cx, float cy, float nx, float ny){

    double PI = 4.0*atan(1.0);

    //main code inside the class
    double dlat1=cx*(PI/180);

    double dlong1=cy*(PI/180);
    double dlat2=nx*(PI/180);
    double dlong2=ny*(PI/180);

    double dLong=dlong1-dlong2;
    double dLat=dlat1-dlat2;

    double aHarv= pow(sin(dLat/2.0),2.0)+cos(dlat1)*cos(dlat2)*pow(sin(dLong/2),2);
    double cHarv=2*atan2(sqrt(aHarv),sqrt(1.0-aHarv));
    //earth's radius from wikipedia varies between 6,356.750 km — 6,378.135 km (˜3,949.901 — 3,963.189 miles)
    //The IUGG value for the equatorial radius of the Earth is 6378.137 km (3963.19 mile)
    const double earth=3963.19;//I am doing miles, just change this to radius in kilometers to get distances in km

    double distance=earth*cHarv;
    return 2*distance;
}

// this is heuristic function for calculating staight line distance between two points
float h( float cx, float cy, float gx, float gy ){
    return straight_distance(cx,cy,gx,gy);
}

class Compare {
public:
    bool operator()(ppi a, ppi b)
    {
        return (a.first.second + h(a.second[a.second.size()-1].first,a.second[a.second.size()-1].second,a.second[0].first,a.second[0].second)) > (b.first.second + h(b.second[b.second.size()-1].first,b.second[b.second.size()-1].second,b.second[0].first,b.second[0].second));
    }
};



// check iff possible to reach to other station with particular batery
bool check(float cx, float cy, float nx, float ny){
    // here we take haversince because it is estimating actual
    float distance = straight_distance(cx,cy,nx,ny);
    // cout<<"]]]"<<cx<<" "<<cy<<" "<< nx<<" "<<ny<<" "<<distance<<endl;
    // here we assume battery to be 90 due to emergency situations
    if( distance < 60*id) return true;
    return false;
}

vector<pair<float,float>> route( float sx, float sy, float gx, float gy, vector<double> poin ){

    // start from start point till goal
    // start from goal point till start
    // we will use A* from both sides to get solution
    // we can get 3 paths atmost
    // 1 from start to goal by A*
    // 2 from goal to start by A*
    // 3 from start to intermidiate and goal to intermidiate by 2 way A*
    // There will be multiple paths with intersection so we will consider all
    // So we will use A* from both sides to get solution
    vector<pair<float,float>> points;
    for(int i=0;i<poin.size();i+=2){
        points.push_back({poin[i],poin[i+1]});
    }
    // Now path

    // priority_queue for expanding paths with minimum heuristic
    priority_queue<ppi,vector<ppi>,Compare> pq;

    // from start to goal
    pq.push({{0,0},{{gx,gy},{sx,sy}}});

    // from goal to start
    pq.push({{1,0},{{sx,sy},{gx,gy}}});

    // visited from start to goal
    map< pair<float,float> ,pair<float,vector<pair<float,float>>> > vis1;

    // visited from goal to start
    map< pair<float,float> ,pair<float,vector<pair<float,float>>> > vis2;

    // final distance
    float path_distance=0;

    // final path
    vector<pair<float,float>> fin_path;

    while(pq.size()>0){
        auto it = pq.top();
        pq.pop();

        // taking top element with smallest heuristic
        int type = it.first.first;
        float actual = it.first.second;
        vector<pair<float,float>> path = it.second;
        float cx = path[path.size()-1].first;
        float cy = path[path.size()-1].second;

        // cout<< cx<<" "<<cy<<" "<<actual<<" "<<type<<endl;

        // for checking visited
        if(type==0){
            // from source side;
            if( vis2.find({cx,cy})!=vis2.end() ){
                // it has been visited by target so go with this as final
//                cout<<actual<<" "<<cx<<" "<<cy<<endl;
                path_distance = actual + vis2[{cx,cy}].first;
                fin_path = path;
                for(int i = vis2[{cx,cy}].second.size()-2; i>=1 ;i-- ){
                    fin_path.push_back(vis2[{cx,cy}].second[i]);
                }
                break;
            }
            else{
                // check if it has been visited by source
                if( vis1.find({cx,cy})!=vis1.end() ){
                    continue;
                }
                // else we will make it visited by source
                vis1[{cx,cy}] = {actual,path};
            }
        }
        else{
            // from target side
            if( vis1.find({cx,cy})!=vis1.end() ){
                // it has been visited by source so go with this as final
                // cout<< vis1[{cx,cy}].first<<" "<<actual<<" "<<cx<<" "<<cy<<endl;
                path_distance = actual + vis1[{cx,cy}].first;
                fin_path = vis1[{cx,cy}].second;
                for(int i = path.size()-2; i>=1 ;i-- ){
                    fin_path.push_back(path[i]);
                }
                break;
            }
            else{
                // check if it has been visited by target
                if( vis2.find({cx,cy})!=vis2.end() ){
                    continue;
                }
                // else we make it visited by target
                vis2[{cx,cy}] = {actual,path};
            }
        }
        // cout<<"----"<< cx<<" "<<cy<<" "<<actual<<" "<<type<<endl;
        // iterating over all point to which we can reach from current state
        for(auto point : points){

            if(!(cx==point.first && cy==point.second) && check(cx,cy,point.first,point.second)){

                // cout<<" //////"<<point.first<<" "<<point.second<<endl;

                // including in path
                path.push_back(point);
                pq.push({{type,actual + straight_distance(cx,cy,point.first,point.second)},path});
                // not including in path
                path.pop_back();
            }
        }
    }

    // now printing all path

    return fin_path;
}


// Write C++ code here.
//
// Do not forget to dynamically load the C++ library into your application.
//
// For instance,
//
// In MainActivity.java:
//    static {
//       System.loadLibrary("evchargingstationlocator");
//    }
//
// Or, in MainActivity.kt:
//    companion object {
//      init {
//         System.loadLibrary("evchargingstationlocator")
//      }
//    }
extern "C"
JNIEXPORT jdoubleArray JNICALL
Java_com_example_evchargingstationlocator_PathFinder_findOptimalPath(JNIEnv *env, jobject thiz,
                                                                     jdouble start_lat,
                                                                     jdouble start_lon,
                                                                     jdouble end_lat,
                                                                     jdouble end_lon,
                                                                     jdoubleArray petrol_stations) {
    // TODO: implement findOptimalPath()
    jsize len = env->GetArrayLength(petrol_stations);
    std::vector<jdouble> petrolStationsVec(len);
    env->GetDoubleArrayRegion(petrol_stations, 0, len, petrolStationsVec.data());

    // Process the received inputs and calculate the optimal path
    // Example usage:
    std::vector<std::pair<double, double>> path = route(start_lat, start_lon, end_lat, end_lon, petrolStationsVec);

    // Convert the C++ vector to a Java double array
    jdoubleArray resultArray = env->NewDoubleArray(path.size() * 2);
    if (resultArray != nullptr) {
        jdouble* resultArrayElements = env->GetDoubleArrayElements(resultArray, nullptr);
        for (int i = 0; i < path.size(); ++i) {
            resultArrayElements[i * 2] = path[i].first;
            resultArrayElements[i * 2 + 1] = path[i].second;
        }
        env->ReleaseDoubleArrayElements(resultArray, resultArrayElements, 0);
    }

    return resultArray;
}

