package graphcontroller.components.ops

import graphcontroller.components.RenderOp
import org.scalajs.dom

/**
 * A generic RenderOp for setting an attribute on a DOM element.
 *
 * @param elementId The ID of the DOM element.
 * @param attribute The name of the attribute to set.
 * @param value     The value to set for the attribute.
 */
case class SetAttribute(elementId: String, attribute: String, value: String) extends RenderOp {
  override def render(): Unit = {
    val elem = dom.document.getElementById(elementId)
    if (elem != null) {
      elem.setAttribute(attribute, value)
    }
  }
}
