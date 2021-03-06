package edu.ist.psu.sagnik.research.pdfbox2playground.javatest;

import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by schoudhury on 6/16/16.
 */
public class LinePathFinder extends PDFGraphicsStreamEngine implements Iterable<Path>
{
    public int index=0;

    public LinePathFinder(PDPage page)
    {
        super(page);
    }

    //
    // PDFGraphicsStreamEngine overrides
    //
    public void findLinePaths() throws IOException
    {
        processPage(getPage());
    }

    @Override
    public void appendRectangle(Point2D p0, Point2D p1, Point2D p2, Point2D p3) throws IOException
    {
        index+=1;
        startPathIfNecessary();
        currentPath.appendRectangle(toFloat(p0), toFloat(p1), toFloat(p2), toFloat(p3),getGraphicsState().getCurrentTransformationMatrix());
    }

    @Override
    public void drawImage(PDImage pdImage) throws IOException { index+=1;}

    @Override
    public void clip(int windingRule) throws IOException
    {
        index+=1;
        currentPath.complete(windingRule);
        //paths.add(currentPath);
        //currentPath = null;

    }

    @Override
    public void moveTo(float x, float y) throws IOException
    {
        index+=1;
        startPathIfNecessary();
        currentPath.moveTo(x, y);
    }

    @Override
    public void lineTo(float x, float y) throws IOException
    {
        index+=1;
        currentPath.lineTo(x, y, getGraphicsState().getCurrentTransformationMatrix());
    }

    @Override
    public void curveTo(float x1, float y1, float x2, float y2, float x3, float y3) throws IOException
    {
        System.out.println("[index]: "+index);
        System.out.println(getGraphicsState().getCurrentTransformationMatrix());
        index+=1;
        currentPath.curveTo(x1, y1, x2, y2, x3, y3,  getGraphicsState().getCurrentTransformationMatrix());
    }

    @Override
    public Point2D.Float getCurrentPoint() throws IOException
    {
        index+=1;
        return currentPath.getCurrentPoint();
    }

    @Override
    public void closePath() throws IOException
    {
        index+=1;
        currentPath.closePath(getGraphicsState().getCurrentTransformationMatrix());
    }

    @Override
    public void endPath() throws IOException
    {
        //paths.add(currentPath); //this has no effect because `n` always follows `W`
        // or `W*`. We are setting currentPath to null in those cases anyway.
        index+=1;
        currentPath = null;
    }

    @Override
    public void strokePath() throws IOException
    {
        index+=1;
        currentPath.complete(-2);
        paths.add(currentPath);
        currentPath = null;
    }

    @Override
    public void fillPath(int windingRule) throws IOException
    {
        index+=1;
        currentPath.complete(-2);
        paths.add(currentPath);
        currentPath = null;
    }

    @Override
    public void fillAndStrokePath(int windingRule) throws IOException
    {
        index+=1;
        currentPath.complete(-2);
        paths.add(currentPath);
        currentPath = null;
    }

    @Override
    public void shadingFill(COSName shadingName) throws IOException
    {
        index+=1;
        currentPath.complete(-2);
        paths.add(currentPath);
        currentPath = null;
    }

    void startPathIfNecessary()
    {
        index+=1;
        if (currentPath == null)
            currentPath = new Path();
    }

    Point2D.Float toFloat(Point2D p)
    {
        if (p == null || (p instanceof Point2D.Float))
        {
            return (Point2D.Float)p;
        }
        return new Point2D.Float((float)p.getX(), (float)p.getY());
    }

    //
    // Iterable<Path> implementation
    //
    public Iterator<Path> iterator()
    {
        return paths.iterator();
    }

    Path currentPath = null;
    final List<Path> paths = new ArrayList<Path>();
}