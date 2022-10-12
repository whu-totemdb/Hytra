package edu.whu.hyk;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Point;
import org.junit.Test;
import rx.Observable;

import static com.github.davidmoten.rtree.geometry.Geometries.*;
public class RtreeTest {


    @Test
    public void test(){
        RTree<String, Point> tree = RTree.maxChildren(5).create();
        tree = tree.add("DAVE", point(10, 20))
                .add("FRED", point(12, 25))
                .add("MARY", point(97, 125));

        Observable<Entry<String, Point>> entries =
                tree.search(Geometries.rectangle(8, 15, 30, 35));
    }
}
