package edu.ist.psu.sagnik.research.pdfbox2playground.text.impl

import java.io.{ByteArrayOutputStream, IOException, OutputStreamWriter}
import java.util

import edu.ist.psu.sagnik.research.pdfbox2playground.model.Rectangle
import edu.ist.psu.sagnik.research.pdfbox2playground.path.impl.CreatePathStyle
import edu.ist.psu.sagnik.research.pdfbox2playground.text.model._
import org.apache.pdfbox.pdmodel.{PDDocument, PDPage}
import org.apache.pdfbox.text.{PDFTextStripper, TextPosition}

import scala.collection.JavaConverters._
import scala.xml.Document

/**
  * Created by schoudhury on 6/27/16.
  */
class ProcessText(page:PDPage) extends PDFTextStripper {

  var currentParagraphs = List.empty[PDParagraph]
  var currentTextLines=List.empty[PDTextLine]
  var currentWords=List.empty[PDWord]
  var currentChars=List.empty[PDChar]


  @Override @throws[IOException]
  override protected def writeWordSeparator(): Unit = {
    if (!"".equals(currentChars.foldLeft("")((a, b) => a + b.content))) { //do not add an empty word
      currentWords = CalculateBB(currentChars) match {
        case Some(bb) =>
          currentWords :+ PDWord(
            content = currentChars.foldLeft("")((a, b) => a + b.content),
            chars = currentChars,
            bb = bb
          )
        case _ => currentWords
      }
    }
    currentChars = List.empty[PDChar]
  }

  @Override @throws[IOException]
  override protected def writeLineSeparator(): Unit = {
    this.writeWordSeparator()
    if (!"".equals(currentWords.foldLeft("")((a, b) => a + b.content + " ").trim)) {
      currentTextLines = CalculateBB(currentWords) match {
        case Some(bb) => currentTextLines :+ PDTextLine(
          content = currentWords.foldLeft("")((a, b) => a + b.content + " "),
          tWords = currentWords,
          bb = bb
        )
        case _ => currentTextLines
      }
    }
    currentWords = List.empty[PDWord]
  }

  @Override @throws[IOException]
  override protected def writeParagraphEnd(): Unit = {
    if (!"".equals(currentTextLines.foldLeft("")((a, b) => a + b.content + " ").trim)) {
      currentParagraphs= CalculateBB(currentTextLines) match {
        case Some(bb)=> currentParagraphs :+ PDParagraph (
          content = currentTextLines.foldLeft ("") ((a, b) => a + b.content + "\n"),
          tLines = currentTextLines,
          bb = bb
        )
        case _ => currentParagraphs
      }
    }
    currentTextLines = List.empty[PDTextLine]
  }

  protected def wordFromTextPositions(tPs:List[TextPosition]):Option[PDWord]={
    val chars=tPs.map(x=>
      PDChar(
        content=x.getUnicode,
        bb=TextPositionBB.approximate(x), // we can change it to other functions. See org.apache.pdfbox.examples.util.DrawPrintTextLocations
        glyphBB=TextPositionBB.glyphBased(x,page),
        style=CreateTextStyle(x,getGraphicsState)
      )
    )
    if (!"".equals((chars.foldLeft("")((a,b)=>a+b.content)).trim))
      CalculateBB(chars) match {
        case Some(bb) => Some (
          PDWord (
            content = chars.foldLeft ("") ((a, b) => a + b.content),
            bb = bb,
            chars
          )
        )
        case _ => None
      }
    else None
  }


  /*@Override @throws[IOException]
  override protected def writeString(s: String, textPositions: util.List[TextPosition]): Unit = {
    //this has to be done because sometimes the writeLine() method is not calling the writeWords() method at all, especially
    //when the string has space characters.
    val tPs=textPositions.asScala.toList
    tPs.foreach(tP=>{
      if (!" ".equals(tP)){
        currentChars=currentChars :+ PDChar(
          content=tP.getUnicode,
          bb=TextPositionBB.approximate(tP), // we can change it to other functions.
          font=tP.getFont
        )
      }
      else
        writeWordSeparator()
    })
  }
*/
  @Override @throws[IOException]
  override protected def writeString(s: String, textPositions: util.List[TextPosition]): Unit = {
    //this has to be done because sometimes the writeLine() method is not calling the writeWords() method at all, especially
    //when the string has space characters.
    val tPss=textPositions.asScala.toList
      .foldLeft(List(List.empty[TextPosition])) {
        (acc, tP) =>
          if (" ".equals(tP.getUnicode)) acc :+ List.empty[TextPosition]
          else acc.init :+ (acc.last :+ tP)
      }
    //if (tPss.length>1) println(tPss)
    tPss.init.foreach(tPs=> currentWords= wordFromTextPositions(tPs) match{
      case Some(w)=>currentWords :+ w
      case _ => currentWords
    })
    tPss.last.foreach(x=>currentChars=currentChars :+
      PDChar(
        content=x.getUnicode,
        bb=TextPositionBB.approximate(x),
        glyphBB=TextPositionBB.glyphBased(x,page),
        CreateTextStyle(x,getGraphicsState)
      )
    )
  }

  def stripPage(pdPageNum: Int, document: PDDocument): List[PDParagraph] = {
    setStartPage(pdPageNum + 1);
    setEndPage(pdPageNum + 1);
    val dummyOutput = new OutputStreamWriter(new ByteArrayOutputStream())
    writeText(document, dummyOutput)
    currentParagraphs
  }
}
