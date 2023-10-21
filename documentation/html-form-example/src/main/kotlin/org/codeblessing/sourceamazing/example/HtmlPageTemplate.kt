package org.codeblessing.sourceamazing.example

object HtmlPageTemplate {

    fun createHtmlPageTemplate(htmlPageConcept: HtmlPageConcept): String {
        val sb =  StringBuilder()
        sb.append("<!DOCTYPE html>\n")
        sb.append("<html>\n")
        sb.append("<head><title>${htmlPageConcept.getHtmlPageTitle()}</title></head>\n")
        sb.append("<body>\n")
        sb.append("<form>\n")
        htmlPageConcept.getSectionsOfPage().forEach { htmlSection ->
            sb.append("<section>\n")
            htmlSection.getSectionName()?.let {sectionTitle ->
                sb.append("<h2>${sectionTitle}</h2>\n")
            }
            htmlSection.getFieldsInSection().forEach { htmlInputField ->
                sb.append("<label for='${htmlInputField.getFieldName()}'>${htmlInputField.getFieldName()}</label>\n")
                sb.append("<input id='${htmlInputField.getFieldName()}' type='text' ${if (htmlInputField.isInputRequired()) "required" else ""} maxlength='${htmlInputField.getMaxFieldLength()}' />\n")
            }
            sb.append("</section>\n")
        }
        sb.append("</form>\n")
        sb.append("</body>\n")
        sb.append("</html>\n")
        return sb.toString()
    }

}
