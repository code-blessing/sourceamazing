# Keyword Glossary

### Domain Unit
The entry point class where your domain schema, the concepts, the templates 
and the files to write are defined.

### Domain Schema

The _domain schema_ holds the all the _concepts_.

### Concept
The _concept_ is the main element to structure items 
in a hierarchical way. A _concept_ may have a parent _concept_ 
and may have one or multiple child _concepts_.

With a _concept_, you can represent whatever you can build code for, like ...
* A database table.
* A database field (as a child of a database table).
* A UI (=user interface) section to group multiple fields together.
* A field in the UI (e.g. as child element of an ui section to group fields).
* A description of a cache.
* A description of a technical interface like a REST-Endpoint.


### Facet

A _facet_ make it possible to add all kind of values to a concept. 
Each _facet_ belongs exactly to one _concept_.

Examples:
If we have the _concept_ "SqlDatabaseTable", we want to add a 
facet "tableName" to provide a name for the database table.
If we have the _concept_ "SqlDatabaseField", we want to add a 
facet "fieldName" to provide a name for the database field and 
another facet "fieldType" to provide information whether this 
database field contains text, numbers or timestamps.

The facet itself can have only the following values:
* A text
* A boolean (=yes/no) value
* A number value. Only non-floating values are supported.
* An enumeration, that means one value from a set of predefined values.
* A reference to another concept

### Template

A _template_ describes the text of a file that is generated. It contains 
placeholders that will be replaced with concrete values. 
Templates can generate all kind of files, e.g. a java file, XML or JSON files, 
an HTML file, etc.

With a _template_, you describe the real code of a file like ...
* an SQL database script with CREATE TABLE statements. 
The names of the tables are placeholders that will be filled out dynamically.
* a java DAO file that contains the fields of a database table and is named 
with the table name.
* an angular or react component to visualize a certain text field like the 
firstname or lastname of a person.
* an angular or react component to group different field 
(firstname and lastname of a person) together as a UI section.
* an angular or react component to provide a navigation for a 
list of navigation entries.
* ... etc., etc.
