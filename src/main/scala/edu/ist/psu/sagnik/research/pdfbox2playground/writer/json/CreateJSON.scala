package edu.ist.psu.sagnik.research.pdfbox2playground.writer.json

import java.awt.geom.Point2D

import edu.ist.psu.sagnik.research.pdfbox2playground.model.PDDocumentSimple
import edu.ist.psu.sagnik.research.pdfbox2playground.model.Rectangle
import edu.ist.psu.sagnik.research.pdfbox2playground.path.model.PDLine
import org.json4s.JsonDSL._
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._

/**
  * Created by schoudhury on 7/6/16.
  */
object CreateJSON {

  def precReduce(d:Float):Float=BigDecimal(d).setScale(2, BigDecimal.RoundingMode.HALF_UP).toFloat

  def precReduce(point:Point2D.Float):List[Float]=List(precReduce(point.x),precReduce(point.y))

  def precReduce(r:Rectangle):List[Float]=List(precReduce(r.x1),precReduce(r.y1),precReduce(r.x2),precReduce(r.x1))

  case class PathStyle(
                        fill:Option[String],
                        fillRule:Option[String],
                        fillOpacity:Option[String],
                        stroke:Option[String],
                        strokeWidth:Option[String],
                        strokeLineCap:Option[String],
                        strokeLineJoin:Option[String],
                        strokeMiterLimit:Option[String],
                        strokeDashArray:Option[String],
                        strokeDashOffset:Option[String],
                        strokeOpacity:Option[String]
                      )

  def apply(jsonLoc:String,pDS: PDDocumentSimple)= {
    val jsonContent =
      (
        "pages" ->
          pDS.pages.map {
            p =>
              (
                ("pageNumber" -> p.pNum) ~
                  ("pageBB" -> precReduce(p.bb)) ~
                  ("rasters" ->
                    p.rasters.map {
                      r => (
                        ("bb" -> precReduce(r.bb))
                        )
                    }
                    ) ~
                  ("graphicsPaths" ->
                    p.gPaths.map {
                      pg => (
                        ("isClippingPath" -> pg.isClip) ~
                          ("doPaint" -> pg.doPaint) ~
                          ("windingRule" -> pg.windingRule) ~
                          /*
                          avoiding path styles to reduce complexity
                          ("pathStyle" -> (
                            ("fill" -> pg.pathStyle.fill) ~
                              ("fillRule" -> pg.pathStyle.fillRule) ~
                              ("fillOpacity" -> pg.pathStyle.fillRule) ~
                              ("stroke" -> pg.pathStyle.stroke) ~
                              ("strokeWidth" -> pg.pathStyle.strokeWidth) ~
                              ("strokeLineCap" -> pg.pathStyle.strokeLineCap) ~
                              ("strokeLineJoin" -> pg.pathStyle.strokeLineJoin) ~
                              ("strokeMiterLimit" -> pg.pathStyle.strokeMiterLimit) ~
                              ("strokeDashArray" -> pg.pathStyle.strokeDashArray) ~
                              ("strokeDashoffset" -> pg.pathStyle.strokeDashOffset) ~
                              ("strokeOpacity" -> pg.pathStyle.strokeOpacity)
                            )
                            ) ~
                            */
                          ("pathSegments" -> pg.subPaths.flatMap(_.segments).map {
                            pgs => (
                              ("start" -> precReduce(pgs.startPoint)) ~
                                ("end" -> precReduce(pgs.endPoint)) ~
                                ("bb" -> precReduce(pgs.bb)) ~
                                ("isLine" -> pgs.isInstanceOf[PDLine]) // a path segment is either a line or a curve
                              )
                          }
                            )
                        )
                    }
                    ) ~
                  ("text" -> p.paragraphs.map{
                    pp =>(
                      ("bb" -> precReduce(pp.bb))~
                        ("content" -> pp.content)
                      )
                  }
                    )
                )
          }
        )
  }
}
