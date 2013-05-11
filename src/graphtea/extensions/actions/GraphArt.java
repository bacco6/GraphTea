package graphtea.extensions.actions;

import graphtea.extensions.io.LoadSimpleGraph;
import graphtea.extensions.io.SaveSimpleGraph;
import graphtea.graph.graph.*;
import graphtea.graph.old.GStroke;
import graphtea.library.BaseVertex;
import graphtea.plugins.main.GraphData;
import graphtea.plugins.main.core.AlgorithmUtils;
import graphtea.plugins.main.extension.GraphActionExtension;
import graphtea.plugins.main.saveload.core.GraphIOException;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.QuadCurve2D;
import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: rostam
 * Date: 5/10/13
 * Time: 7:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class GraphArt implements GraphActionExtension {
    static final String CURVE_WIDTH = "Curve Width";
    @Override
    public String getName() {
        return "GraphTea Art";
    }

    @Override
    public String getDescription() {
        return "GraphTea Art";
    }

    @Override
    public void action(GraphData graphData) {
        Vertex.addGlobalUserDefinedAttribute(CURVE_WIDTH,5);

        GraphModel g1 = graphData.getGraph();
        GraphModel g2 = null;
        SaveSimpleGraph ssg = new SaveSimpleGraph();
        LoadSimpleGraph lsg = new LoadSimpleGraph();
        try {
            ssg.write(new File("tmpg1"),g1);
            g2 = lsg.read(new File("tmpg1"));
        } catch (GraphIOException e) {
            e.printStackTrace();
        }
        g2.setFont(new Font(g2.getFont().getName(),g2.getFont().getStyle(), 0));
        g2.setLabel("TreeG0");

        for(Vertex v : g2)
            v.setSize(new GraphPoint(15,15));
        for(Edge e : g2.getEdges()) {
            e.setStroke(GStroke.dashed_dotted);
            e.setColor(8);
        }

        graphData.core.showGraph(g2);
        Painter p = new Painter(graphData);
        AbstractGraphRenderer gr = AbstractGraphRenderer.getCurrentGraphRenderer(graphData.getBlackboard());
        gr.addPostPaintHandler(new Painter(graphData));
        gr.repaint();
    }
}


class Painter implements PaintHandler {
    GraphData gd;
    GraphModel G;
    Integer curveWidth = 6;
    public Painter(GraphData gd) {
        this.gd = gd;
        this.G  = gd.getGraph();
    }

    public void paint(Graphics gr1d, Object destinationComponent, Boolean b) {
        final Graphics2D gr = (Graphics2D) gr1d;
        final int n = G.getVerticesCount();
        if (n == 0) return;

        AbstractGraphRenderer.getCurrentGraphRenderer(gd.getBlackboard()).ignoreRepaints(new Runnable() {
            public void run() {
                boolean[] marks = new boolean[n];
                Vertex V[] = G.getVertexArray();
                final Vertex parent[] = new Vertex[n];

                //consider the hole structure as a tree
                AlgorithmUtils.BFSrun(G, V[0], new AlgorithmUtils.BFSListener() {
                    @Override
                    public void visit(BaseVertex v, BaseVertex p) {
                        parent[v.getId()] = (Vertex)p;
                    }
                });


                for (Vertex v : G) {
                    if (v.getId() == 0) continue;
                    if (v.getColor() == 0) {
                        Vertex v1 = parent[v.getId()];
                        if (v1 == null || v1.getColor() != 0) continue;

                        Vertex v2 = parent[v1.getId()];
                        if (v2 == null || v2.getColor() != 0) continue;

                        //generate the curve between v1, v2 and v3
                        GraphPoint p1 = v.getLocation();
                        GraphPoint p2 = v1.getLocation();
                        GraphPoint p3 = v2.getLocation();

                        GraphPoint m1 = AlgorithmUtils.getMiddlePoint(p1, p2);
                        GraphPoint m2 = AlgorithmUtils.getMiddlePoint(p2, p3);
                        GraphPoint cp = p2;

                        Integer w1 = v.getUserDefinedAttribute(GraphArt.CURVE_WIDTH);
                        Integer w2 = v1.getUserDefinedAttribute(GraphArt.CURVE_WIDTH);
                        Integer w3 = v2.getUserDefinedAttribute(GraphArt.CURVE_WIDTH);

                        int startWidth = (w1 + w2) / 2;
                        int endWidth = (w3 + w2) / 2;
                        int middleWidth = w2;

                        double teta1 = AlgorithmUtils.getAngle(p1, p2);
                        double teta2 = AlgorithmUtils.getAngle(p1, p3);
                        double teta3 = AlgorithmUtils.getAngle(p2, p3);

                        //generate boundary curves
                        java.awt.geom.QuadCurve2D c1 = new QuadCurve2D.Double(
                                m1.x - startWidth * Math.sin(teta1), m1.y + startWidth * Math.cos(teta1),
                                cp.x - middleWidth * Math.sin(teta2), cp.y + middleWidth * Math.cos(teta2),
                                m2.x - endWidth * Math.sin(teta3), m2.y + endWidth * Math.cos(teta3));

                        java.awt.geom.QuadCurve2D c2 = new QuadCurve2D.Double(
                                m2.x + endWidth * Math.sin(teta3), m2.y - endWidth * Math.cos(teta3),
                                cp.x + middleWidth * Math.sin(teta2), cp.y - middleWidth * Math.cos(teta2),
                                m1.x + startWidth * Math.sin(teta1), m1.y - startWidth * Math.cos(teta1));

                        //mix them
                        GeneralPath gp = new GeneralPath(c1);
                        gp.append(c2, true);
                        gp.closePath();
                        gr.setColor(new Color(50,100,50));

                        //fill the curve
                        gr.fill(gp);
                    }
                }
            }
        }, false /* dont repaint after*/);
    }
}
