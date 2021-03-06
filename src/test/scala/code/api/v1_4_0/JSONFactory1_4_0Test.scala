package code.api.v1_4_0

import java.util.Date

import code.api.ResourceDocs1_4_0.SwaggerDefinitionsJSON
import code.api.util.APIUtil
import code.api.util.APIUtil.{ApiVersion, ResourceDoc}
import code.api.v1_4_0.JSONFactory1_4_0.ResourceDocJson
import code.api.v2_1_0.OBPAPI2_1_0
import code.api.v2_2_0.OBPAPI2_2_0
import code.api.v3_0_0.OBPAPI3_0_0
import code.util.Helper.MdcLoggable
import net.liftweb.json.Extraction
import org.scalatest._

import scala.collection.mutable.ArrayBuffer
import net.liftweb.json._

import scala.collection.mutable

case class OneClass(
  string : String = "String",
  strings : List[String] = List("String")
)

case class AllCases(
  string: String = "String",
  strings: List[String] = List("String"),
  date: Date = APIUtil.exampleDate,
  dates: List[Date] = List(APIUtil.exampleDate),
  boolean: Boolean = true,
  booleans: List[Boolean]= List(true),
  int: Int = 3,
  ints: List[Int]= List(3),
  nestClass: OneClass = OneClass(),
  nestClasses: List[OneClass] = List(OneClass()),
  jvalue: JValue= APIUtil.defaultJValue,
  jvalues: List[JValue]= List(APIUtil.defaultJValue)
)

class JSONFactory1_4_0Test extends FlatSpec
  with Matchers
  with MdcLoggable {
  
  implicit val formats = net.liftweb.json.DefaultFormats
  
  "JSONFactory1_4_0.createResourceDocJson" should "work well, no exception is good enough" in {
    val resourceDoc: ResourceDoc = OBPAPI3_0_0.allResourceDocs(5)
    val result: ResourceDocJson = JSONFactory1_4_0.createResourceDocJson(resourceDoc)
  }
  
  "JSONFactory1_4_0.createResourceDocsJson" should "work well, no exception is good enough" in {
    val resourceDoc: mutable.Seq[ResourceDoc] = OBPAPI3_0_0.allResourceDocs
    val result = JSONFactory1_4_0.createResourceDocsJson(resourceDoc.toList)
  }
  
  
  "JSONFactory1_4_0.createTypedBody" should "work well, no exception is good enough" in {
    val inputCaseClass = AllCases()
    val result = JSONFactory1_4_0.createTypedBody(inputCaseClass)
//    logger.debug(prettyRender(result))
  }
  
}
