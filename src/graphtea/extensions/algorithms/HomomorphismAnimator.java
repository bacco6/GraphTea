package graphtea.extensions.algorithms;

import graphtea.graph.graph.Edge;
import graphtea.graph.graph.GraphModel;
import graphtea.graph.graph.GraphPoint;
import graphtea.graph.graph.Vertex;
import graphtea.library.Path;
import graphtea.platform.core.BlackBoard;
import graphtea.plugins.algorithmanimator.core.GraphAlgorithm;
import graphtea.plugins.algorithmanimator.extension.AlgorithmExtension;
import graphtea.plugins.main.core.AlgorithmUtils;

import javax.swing.*;
import java.awt.geom.Point2D;
import java.util.Vector;

/**
 * author: rostam
 * author: azin
 */
public class HomomorphismAnimator extends GraphAlgorithm implements AlgorithmExtension {
    public HomomorphismAnimator(BlackBoard blackBoard) {
        super(blackBoard);
    }

    @Override
    public void doAlgorithm() {

        GraphModel g = graphData.getGraph();
        step("start the algorithm.");
        String str = JOptionPane.showInputDialog(null,
                "Enter the homomorphism map size: ",
                "Homomorphism Size", 1);
        int mapSize = Integer.parseInt(str);
        Vertex[] vs = new Vertex[2*mapSize];
        GraphPoint[] directions = new GraphPoint[mapSize];
        for(int i=0;i<2*mapSize;i=i+2) {
            vs[i] =   requestVertex(g, "select the" + i + "th vertex.");
            vs[i+1] = requestVertex(g, "select the" + i + "th vertex.");

            if(g.isEdge(vs[i],vs[i+1])) {step("The vertices have edge together.");return;}

            GraphPoint directionVector = GraphPoint.sub(vs[i].getLocation(), vs[i+1].getLocation());
            directions[i/2] = GraphPoint.div(directionVector, directionVector.norm());
        }


        boolean[] isThere = new boolean[mapSize];
        int numOfThere = 0;
        for(int i=0;i < mapSize;i++) isThere[i]=false;
        for(int j=0;j < 100;j++) {
            if(numOfThere == mapSize) break;
            step(j+"th step");

            for(int i=0;i<2*mapSize;i=i+2) {
                if(isThere[i/2]) continue;
                vs[i].setLocation(new GraphPoint(
                        vs[i].getLocation().getX()-directions[i/2].getX() * 10,
                        vs[i].getLocation().getY()+directions[i/2].getY() * 10));

                System.out.println(vs[i].getLocation().distance(vs[i+1].getLocation()));
                if(vs[i].getLocation().distance(vs[i+1].getLocation()) < 8) {
                    isThere[i/2] = true;
                    numOfThere++;
                }
            }
        }
        step("the end of the algorithm.");
    }

    @Override
    public String getName() {
        return "Homomorpihsm Animator ";
    }

    @Override
    public String getDescription() {
        return "The visualization of homomorphism maps in practice";
    }
}