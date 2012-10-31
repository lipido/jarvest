# ### Introduction

# An *jARVEST* Robot specifies steps for doing web scraping of a
# page or a set of pages. Each robot is built from several
# transformers connected in the shape of a graph. Each transformer
# type specifies some elemental operation to make. They are
# parametrized and combined to create a whole robot. This robot is
# specified in a XML format.
#
# Here we define a DSL that we call *minilanguage*. It eases the
# creation of *jARVEST* robots, so no longer is needed to create
# verbose XML files. Instead we can use a *minilanguage* string to
# define it.
#
# We can create a new *jARVEST* robot by feeding a minilanguage
# string to *Language*. *Language* class interprets it and creates a
# tree of *Transformer* objects. This *Transformer*'s tree resembles
# *jARVEST* XML format so the latter is easy to generate.
#
# *Language* interprets the *minilanguage* string by using the `eval`
# capabilities of Ruby. At *Language* class there are methods for
# creating transformers of each indidividual type. In order to not
# have to define manually a method for each type of Transformer, we
# generate them dinamically.

# We include java packages for generating the XML.
# Author: Oscar González Fernández

require 'java'
module XML
  include_package 'javax.xml.parsers'
  include_package 'org.w3c.dom'
end

# ### *Transformer* as template for new sublcasses

# We define methods to ease the definition of new *Transformer*
# subclasses. These methods are called when the class is being
# constructed and parametrize the behavior of the generated
# subclass. Each generated *Transformer* subclass receives the values
# for the parameters it requires when its constructor is
# called. *Transformer* class will be reopened.
class Transformer

# The `@variables` used inside of class methods(`self.method_name`)
# are local to each subclass. They are instance variables of each
# class metaclass.

# This method is intended to be used inside the body of subclasses
# from Transformer. It stores the value of the class as a symbol.
  def self.transformer_class value
    @transformer_class = value.to_sym
  end

  def self.transformer_class_symbol
    @transformer_class
  end

# Register a custom name for this Transformer subclass. By default,
# the generated method name is built from the class name. But
# sometimes that isn't suitable and a custom name must be provided.
  def self.custom_name name
    @custom_name = name
  end

  def self.get_custom_name
    @custom_name
  end

# It stores a new param definition. This method is intended to be used
# inside the body of subclasses from Transformer.
  def self.param(name, hash)
    params_definitions << ParamDefinition.new(name, hash)
  end

  def self.params_definitions
    (@params_definitions||=[])
  end
end

# It stores a param definition. It has a name, if it's required and a
# default value. Each *Transformer* subclass has some different
# *ParamDefinition*.
class ParamDefinition

  def self.default_value paramDefinition
    paramDefinition && paramDefinition.default_value
  end

  attr_accessor :name
  attr_accessor :required
  attr_accessor :default_value

  def initialize(name, params)
    self.name = name.to_sym
    self.required = params[:required] && true
    self.default_value = params[:default_value]
  end
end

# We reopen *Transformer* class to add a hook: `self.inherited` is
# called each time a new subclass of *Transformer* is defined.
#
# This is a partial definition. *Transformer* class will be reopened.
class Transformer
# Whenever a new subclass of *Transformer* is created, we tell
# Language. We also keep track of the subclasses added.
  def self.inherited subclass
    Language.new_type_of_transformer(subclass)
    (@@subclasses ||= []) << subclass
  end

# The name of the method to be added to *Language*. It's created from
# the name of the class with the first letter in lowercase, unless
# it's has been customized.
  def self.language_method_name
    self.get_custom_name || self.lowercase_first(self.to_s)
  end

  def self.lowercase_first word
    word[0, 1].downcase + word[1, word.length - 1]
  end
end

# In *Language* class an instance method is created for each of the
# subclasses of *Transformer*. This is a partial
# definition. *Language* class will be reopened.
class Language

  @@pending_method_definitions = []

# Once a transformer class is created, a method that creates a
# transformer instance of that type is *eventually* added to
# Language. The newly generated method will call
# `transformer_added_action` with the provided parameters.

# We can't define the new method immediately because when the
# `inherited` hook is called the body of the Transformer subclass has
# not been executed yet. `language_method_name` wouldn't still be
# defined.
  def self.new_type_of_transformer transformer_klass
    @@pending_method_definitions << lambda {
      define_method_for transformer_klass
    }
  end

#  For example after defining the class `PatternMatcher` a new method
# `patternMatcher` is defined on *Language* class, once the
# `register_pending_method_definitions` is called. The methods
# generated receive some arguments that will be passed directly to the
# class constructor. If a block is provided, it's evaluated in a new
# *Language* instance.
  def self.define_method_for transformer_klass
    define_method(transformer_klass.language_method_name) do |*args, &block|
      transformer = transformer_klass.new(*args)
      transformer_added_action(transformer)
      if block
        Language.new transformer, &block
      end
      self
    end
  end

# It invokes all the pending method defintions causing the methods for
# each transformer to  be defined.
  def self.register_pending_method_definitions
# This race condition doesn't matter because all transformers are
# defined before languages are started to be instantiated.
    pending = Array.new @@pending_method_definitions
    @@pending_method_definitions = []
    pending.each do |l|
      l.call()
    end
  end

end

# Now let's define some *Transformer* subclasses that will cause
# methods to be added to the minilanguage. Notice how we use
# `transformer_class` and `param` methods defined before to simplify
# the definition of *Transformer* classes.

# It defines the `patternMatcher` call on *Language*. It can be used as
# `patternMatcher('regexHere')` or
# `patternMatcher(:pattern=>'regexHere')`
class PatternMatcher < Transformer
  transformer_class :PatternMatcher
  custom_name "match"
  param :pattern, :required => true
  param :dotAll, :default_value => true
  param :ifNoMatch, :required=>false, :default_value => "--none--"
end

# It defines the `xpath` call on *Language*. It can be used as
# `xpath('//a/@href')` or `xpath(:Xpath->'//a/@href')`
class Xpath < Transformer
  transformer_class :HTMLMatcher
  param :XPath, :required => true
  param :addTBody, :default_value => true
  param :ifNoMatch, :required=>false, :default_value => "--none--"
end

# It defines the `xpath` call on *Language*. It can be used as
# `xpathxml('//a/@href')` or `xpathxml(:Xpath->'//a/@href')`
class Xpathscrap < Transformer
  transformer_class :HTMLMatcherScrap
  param :XPath, :required => true
  param :addTBody, :default_value => true
  param :ifNoMatch, :required=>false, :default_value => "--none--"
end

# This is not intended to be used as a direct call on *Language*. It
# acts as a container of other transformers. For that you should use
# the special calls `pipe` and `branch` that accept blocks.
class SimpleTransformer < Transformer
  transformer_class :SimpleTransformer
end
# It defines the `appender` call on *Language*. It can be used as
# `appender('<br />')` or `appender(:append->'<br />')`
class Appender < Transformer
  custom_name "append"
  transformer_class :Appender
  param :append, :required => true, :default_value=>''
end

# It defines the base class for http based transformers: wget and post
class URLTransformer < Transformer  
  param :ajax, :required => false, :default_value => 'false'
  param :headers, :required => false, :default_value => '{}'  
  param :userAgent, :required => false, :default_value => ''
end

# It defines the `url` call on *Language*. `@@custom_names` on
# transformer class is used to customize the name. It's used as `url`
# or `url(:description=>'whatever')`
class URLRetriever < URLTransformer
  custom_name "wget"
  transformer_class :URLRetriever  
end

# It defines the `post` call on *Language*. `@@custom_names` on
# transformer class is used to customize the name. It's used as `url`
# or `url(:description=>'whatever')`
class Post < URLTransformer
  custom_name "post"
  transformer_class :HTTPPost
  param :querySeparator, :required => false, :default_value => '&'
  param :outputHTTPOutputs, :required => false, :default_value => 'false'
  param :queryString, :required => true
  param :URL, :required => true
end

# It defines the `replacer` call on *Language*. It's used as
# `replacer(:sourceRE=> '', :dest=>'')`
class Replacer < Transformer
  custom_name "replace"
  transformer_class :Replacer
  param :sourceRE, :required => true
  param :dest, :required => true
end

# It defines the `compare` call on *Language*. It's used as
# `compare(:compareWith=> '7', :compareAs=>'Number')`
class Comparator < Transformer
  custom_name "compare"
  transformer_class :Comparator
  param :compareAs, :default_value=>'String'
  param :compareWith, :required=>true
  param :prefixIfGreater, :default_value=>'_GREATER_'
  param :prefixIfLess, :default_value=>'_LESS_'
  param :prefixIfEquals, :default_value=>'_EQUALS_'
  param :prefixIfError, :default_value=>'_ERROR_'
end  
# It defines the `decorator` call on *Language*. It's used as
# `decorator(:head=> '<p>', :tail=>'</p>')`.
class Decorator < Transformer
  custom_name "decorate"
  transformer_class :Decorator
  param :head, :default_value => ''
  param :tail, :default_value => ''
end
# It defines the `merger` call on *Language*. It's used as
# `merger`. It has no required parameters.
class Merger < Transformer
  custom_name "merge"
  transformer_class :Merger
end

# ### *Transformer* instantiation.

# We reopen the *Transformer* class to show how it's instantiated. The
# initialize constructor is called when calling the method dinamically
# generated on *Language* class.

class Transformer

# As you can see `arguments` is a variable list of arguments. It can
# have zero, one, two or three arguments. If branch parameters are
# provided they are the first two. The params are the last one, but
# it's optional.
  def self.split_parameters arguments
    params = (arguments.length == 1 || arguments.length == 3) && arguments.last
    # branch parameters provided
    if arguments.length >= 2
      [arguments[0, 2], params]
    # no branch parameters
    else
      [[], params]
    end
  end

# A transformer constructor. It's called from *Language* when the
# concrete method associated to this class is called. Examples of
# calls on *Language* that can trigger this:
#
#     patternMatcher('(http://.*)')
#     decorator(:head=>"<h1>", :tail=>"</h1>", :description=>"wrapping")
#
  def initialize *arguments
    klass = self.class
# Check that `transformer_class` has been defined
    unless klass.transformer_class_symbol
      raise ArgumentError, "transformer_class must be defined in #{klass}"
    end

    branch_parameters, params = klass.split_parameters arguments
# We ensure the params is in a Hash form, that the branch parameters
# are added, and that if a key is not found the value in
# default_values is returned.
    params = klass.ensure_params_is_hash(params)
    params = klass.with_branch_params(params, branch_parameters)
    params = klass.with_defaults params
# We set instance attributes from the params. Notice that if the value
# is not present, the default values are used.
    @description = params[:description]
    @branchtype = params[:branchtype].to_sym
    @branchmergemode = params[:branchmergemode].to_sym
    @loop = params[:loop]
# We extract the values from the params for the param definitions and
# we generate the accessors.
    @params = (klass.values_for_param_definitions params).freeze
    klass.generate_named_accessors_for_parameters("params")
  end

# The default values used for the standard parameters. If the value
# are equal to these they don't have to be provided.
  @@default_values = {:branchtype => :CASCADE, :branchmergemode => :ORDERED,
    :loop => false}

# Create a new hash with the contents of `hash` and that fallbacks to
# `@@defaults_values` and the param definition if some key is not
# found.
  def self.with_defaults hash
    result = Hash.new {|hash, key|
      if key == :description
        transformer_class_symbol.to_s
      else
        if @@default_values[key]
          @@default_values[key]
        else
          ParamDefinition.default_value(find_param_definition(key))
        end
      end
    }
    result.merge! hash
    result
  end

  def self.find_param_definition name
    params_definitions.find{|p| p.name.to_sym == name.to_sym}
  end

# Add to the params hash the branch params if they exist.
  def self.with_branch_params(hash, branch_params)
    hash[:branchtype] = branch_params[0] if branch_params[0]
    hash[:branchmergemode] = branch_params[1] if branch_params[1]
    hash
  end

# If params is a Hash we return early.
  def self.ensure_params_is_hash provided_params_values
    return provided_params_values if Hash === provided_params_values
# Otherwise we create a new Hash and if there is only one required
# param we set the value of that param for the single value provided.
    single_argument = provided_params_values
    result = {}
    required = params_definitions.find_all {|x| x.required}
    if required.size == 1
      result[required[0].name] = single_argument
    end
    result
  end

# Extracts from the provided params hash, the actual values taking
# into account the default values. It also checks that all the
# required parameters without default values are set.
  def self.values_for_param_definitions provided_params_values
    result = provided_params_values.clone
    params_definitions.each do |p|
      if !result[p.name] && p.required && !p.default_value
        raise ArgumentError, "#{p.name} parameter is required"
      end
      result[p.name] = result[p.name] || p.default_value
    end
    result
  end

# It generates dinamically accessors for the parameters definitions in
# this instance, the values are the ones stored in
# `instance_variable_name`. For example for `PatternMatcher` we
# generate `pattern` and `dotAll` accessors.
  def self.generate_named_accessors_for_parameters instance_variable_name
    params_definitions.each do |param_definition|
      module_eval <<-END
        def #{param_definition.name}
          @#{instance_variable_name}[:#{param_definition.name}]
        end
      END
    end
  end

# Some accessors for properties common for all transformers.

  attr_reader :branchtype
  attr_reader :branchmergemode
  attr_reader :description

  def transformer_class
    self.class.transformer_class_symbol
  end

  def children
    Array.new(@children || [])
  end

  def loop?
    @loop
  end

  def params
    @params
  end
end

# ### *Language* interpretation


# We reopen *Transformer* to add the methods needed by *Language* to
# manipulate each created transformer.
class Transformer

# *jARVEST* expects that the loop control is the first element.
  def do_loop_with loop_control_transformer
    (@children ||= []).insert(0, loop_control_transformer)
    @loop = true
  end
# Append a child to this transformer
  def add_child child
    (@children ||= []) << child
  end

end

# Besides the methods presented here this class contains all the
# methods generated by the *Transformer* subclasses definitions. The
# Language uses Ruby blocks to create nested scopes.
class Language

# This is the entering point for *Language*. A new *Language* instance
# is created, against which the `language_str` is evaluated. For
# example if `language_str` is `url | xpath('//a/@href')` the methods
# `url`, `|` and `xpath`, `|` are executed on the new *Language*
# instance.
  def self.language_eval language_str, filename=nil, lineno=nil
    language = create_top_level
    args = [language_str, filename, lineno].reject{|x| x.nil?}
    language.instance_eval *args
# We return the top level generated transformer
    language.transformer
  end

# The root transformer is a *SimpleTransformer* that works in cascade
# mode.
  def self.create_top_level &block
    Language.new SimpleTransformer.new({}), &block
  end

# Method used in the tests. It's mostly the same as language_eval but
# instead of a string to be evaled it receives a Ruby block of
# code.
  def self.interpret &block
    l = create_top_level &block
    l.transformer
  end

# Method called when a new transformer is created using one of the
# dinamically defined methods. It adds the created transformer
# instance to the current transformer. For example, calling
# `patternMatcher('regexHere')` on some *Language* scope adds it as a
# child to this Language's instance `@transformer`.
  def transformer_added_action transformer
    @transformer.add_child transformer
  end
  protected :transformer_added_action

# This is syntactic sugar. Inside the top level scope or a pipe scope
# the `|` separator can be used to separate several transformer
# instantiations. The Ruby statement separator `;` can be used too,
# but `|` gives it a 'flowing' look.
  def > other
    self | other
  end

  def | other
    if @transformer.branchtype != :CASCADE
      raise "| can't be used in a pipe branch"
    end
    self
  end

# Constructor for a *Language*. Both params and block are optional. It
# creates a `SimpleTransformer` as container. It interprets the
# optionally provided block in the context of the *Language* being
# created. It also registers the pending method definitions to ensure
# that they are available.
  def initialize transformer, &block
    Language.register_pending_method_definitions
    @transformer = transformer
    instance_eval(&block) if block
  end

# The transformer that is being modified in this `Language` instance.
  def transformer
    @transformer
  end

# With what we have shown so far we only could create transformers
# that don't have other nested transformers. Now we define some
# methods that let us create nested scopes inside a Language. Each
# scope is a *Language* instance so more nested scopes can be created
# recursively. This scope methods receive Ruby blocks. Ruby blocks can
# be delimited with braces which is a very readable nesting construct.

# It creates a new minilanguage scope with pipe semantics. For example
# a pipe could be:
#
#     url | pipe {xpath('//a/@href') |
#                 patternMatcher('(http://.*)')}
#
# A transformer with two children would be created: url and a
# SimpleTransformer with xpath and patternMatcher as children:
#
#     SimpleTransformer :branchtype => :CASCADE
#       - url
#       - SimpleTransformer :branchtype => :CASCADE
#         - xpath
#         - patternMatcher
#
  def pipe &block
    pipe = Language.new SimpleTransformer.new, &block
    @transformer.add_child pipe.transformer
    self
  end

# It creates a new minilanguage scope with one_to_one semantics. 
# Treats each input of the parent independently, and ensures only
# <=1 outputs per input. This is done by creating a SimpleTransformer
# with branchtype => BRANCH_SCATTERED, with only one child
# (a SimpleTransformer) containing the internal block, and by adding a
# merger at the end of the internal block. An example would be:
# 
#     wget | xpath('//a/@href') | one_to_one {
#		xpath('//tr/td/text()') | decorate(:tail=>'\n')
#	  }
#	  decorate(:head=>'[site_open]', :tail=>'[site_close'])
#
  def one_to_one &block
    one_to_one = Language.new SimpleTransformer.new(:BRANCH_SCATTERED, :ORDERED)
    inside = Language.new SimpleTransformer.new, &block
    inside.transformer.add_child (Language.new Merger.new).transformer
    one_to_one.transformer.add_child inside.transformer
    @transformer.add_child one_to_one.transformer
    self
  end
  
# It creates a new minilanguage scope with branch semantics. A branch
# requires that its type and merge mode are specified as params. The
# provided block is interpreted with the newly created instance. The
# children of a branch must be separed by newlines, never by `|`. An
# example of branch is:
#
#     branch(:BRANCH_DUPLICATED, :COLLAPSED) {
#       decorator(:head=>"<h1>", :tail => "</h1>")
#       url
#     }
# It would generate:
#
#     SimpleTransformer :branchtype => :BRANCH_DUPLICATED
#                       :branchmergemode => :SCATTERED
#       - decorator :head => "<h1>", :tail => "</h1>"
#       - url
#
  def branch *params, &block
    unless params[0] && params[1]
      raise ArgumentError, "branchtype and branchmerge mode required"
    end
    branch = Language.new(SimpleTransformer.new(*params), &block)
    @transformer.add_child branch.transformer
    self
  end

# It tells that the last defined transformer is executed in a loop. The
# provided block is used as the loop control. For example:
#
#     url { patternMatcher(:pattern=>"somePattern")}.repeat? {
#                 patternMatcher(:pattern=>"somePattern") |
#                 decorator(:head=>"someURL")}
#  
# Notice how a transformer can receive other as children. By default
# they are in cascade mode (the same as `pipe`). This is specially
# useful with `repeat?` blocks.
  def repeat? *params, &block
    last_transformer = @transformer.children.last
    unless last_transformer
      raise "no transformer to which apply a repeat? clause"
    end
    clause = RepeatClause.new(SimpleTransformer.new(*params), &block)
    if clause.transformer
      last_transformer.do_loop_with clause.transformer
    end
    self
  end
end

# It interprets the parts inside of a `repeat?` block.
class RepeatClause < Language

  alias_method :standard_added_action, :transformer_added_action

# Instead of adding the created transformer to the container the
# children of the repeat clase are stored.
  def transformer_added_action transformer
    (@transformers||=[]) << transformer
  end

# When we want to retrieve the transformer we look at the transformers
# that have been added.
  def transformer
# We add them to the implicit container first.
    (@transformers || []).each do |t|
      standard_added_action t
    end
    @transformers.clear if @transformers
# If only one transformer instantiated on the repeat clause we return
# it. Otherwise we return the implicit container.
    if super.children.size == 1
      super.children[0]
    else
      super
    end
  end
end

# ### *jARVEST* XML building

# It exposes methods to ease the creation of the XML for an
# *jARVEST* *robot*. New *XMLBuilder* instances are created for
# each transformer node.
class XMLBuilder

# It creates a new XML Document
  def self.create
    factory = XML::DocumentBuilderFactory.newInstance
    factory.setNamespaceAware false
    XMLBuilder.new(factory.newDocumentBuilder.newDocument)
  end

# A *XMLBuilder* must have a reference to the document and the node
# it's acting on. At the start that's the root document.
  def initialize(doc, node=doc)
    @doc = doc
    @node = node
  end

# When adding a new element a new *XMLBuilder* is created so that
# element can be subsequently modified.
  def add_element name
    element = @doc.createElement(name)
    @node.appendChild(element)
    XMLBuilder.new(@doc,element)
  end

# Using *AttributesWrapper* to simplify attribute modification
  def attributes
    @attributes_wrapper ||= AttributesWrapper.new(@node)
  end

  def document
    @doc
  end

  def node
    @node
  end
  protected :node

# Add param element to transformer element with the provided value
  def add_param(key, value)
    param_element = self.add_element 'param'
    param_element.attributes['key']= key.to_s
    text = @doc.createTextNode(value.to_s)
    param_element.node.appendChild(text)
    param_element
  end
end

# It eases the way of setting attributes to an XML Element
class AttributesWrapper
  def initialize(element)
    @element = element
  end

  def []=(name,value)
    @element.setAttribute(name.to_s,value)
  end
end

# Method to be called from the Java side with the language
# string. It's interpreted by *Language* and the transformer generated
# along its children is converted to XML.
def get_xml str, filename=nil, lineno=nil
  transformer = Language.language_eval(str, filename, lineno)
  transformer.convert_to_xml
end

# We reopen the *Transformer* class to define the methods needed to
# convert to xml.
class Transformer

  def convert_to_xml
    builder = XMLBuilder.create
    robot = builder.add_element 'robot'
    robot.attributes['version'] = '1.0'
    self.fill_transformer_element(robot.add_element('transformer'))
    builder.document
  end

# It fills the properties needed for an *jARVEST* transformer
  def fill_transformer_element builder
# We set the attributes.
    builder.attributes['class'] = self.class.transformer_class_symbol.to_s
    builder.attributes['branchtype'] = branchtype.to_s
    builder.attributes['branchmergemode'] = branchmergemode.to_s
    builder.attributes['loop'] = loop?.to_s
# We add the params as elements.
    params.each_pair do |key, value|
      builder.add_param(key, value)
    end
# We apply this same method recursively to the children
    children.each do |child|
      child.fill_transformer_element(builder.add_element("transformer"))
    end
    builder
  end
end

# ### From *jARVEST* XML to minilanguage

# It takes an XML in *jARVEST* format and generates a minilanguage
# string that would generate it.
def to_minilanguage xml
# The document element is robot and its children are the top level transformers
  children_to_minilanguage(xml.getDocumentElement, " | ")
end

# It converts the transformer children of an element to minilanguage.
def children_to_minilanguage parent, separator
  result = ""

# Lambda that converts an element to minilanguate taking into account
# its position.
  generate_element = lambda do |element, index|
    # We retrieve the Transformer class associated to the element.
    transformer_class = Transformer.get_class_for(element.getAttribute("class"))
    unless transformer_class
      raise "not found class for #{element.getAttribute('class')}"
    end

    result << separator if index > 0
# The retrieved transformer_class knows how to convert itself to
# minilanguage. We provide a block that knows how to generate the
# children. It basically calls this method recursively.
    result << transformer_class.to_minilanguage(element) {|sep|
      children_to_minilanguage(element, sep)
    }
    loop_control = get_loop_control(element)
    unless loop_control.empty?
      result << ".repeat?{\n"
      loop_control.each_with_index &generate_element
      result << "}"
    end
  end
  get_transformers(parent).each_with_index &generate_element
  result
end

# If the transformer has the loop property the first transformer will
# work as loop control. Otherwise all work as standard transformers.
def partition_transformers parent
  all = children_list(parent).select_with_name("transformer").to_a
  is_loop = parent.getAttribute('loop') =~ /true/
  if is_loop
    [all[0, 1], all[1, all.length - 1]]
  else
     [[], all]
  end
end

def get_transformers parent
  partition_transformers(parent)[1]
end

def get_loop_control parent
  partition_transformers(parent)[0]
end

# We reopen *Transformer* class to add the parts for converting XML to
# minilanguage.
class Transformer

# For a given transformer class name (the one without the package) and
# present in the class attribute of the transformer element in
# *jARVEST's* XML. It returns the appropriate subclass of
# *Transformer*
  def self.get_class_for simple_java_class_name
    subclasses_by_name[simple_java_class_name.to_sym]
  end

  def self.subclasses_by_name
    @@subclasses.inject(Hash.new) {|acc, subclass|
      acc[subclass.transformer_class_symbol] = subclass
      acc
    }
  end

# It converts the provided transformer element to a subpart of the
# resulting minilanguage. For generating the children we use
# `children_content`.
  def self.to_minilanguage element, &children_content
# We extract the params to use.
    params = with_defaults(extract_params_from_element(element))
# If no further transformer children we generate the call.
    loop_control, children = partition_transformers(element)
    if children.empty?
      as_call(params)
# If both branch parameters are equal to the default values it's a pipe.
    else
      is_pipe = [:branchtype, :branchmergemode].all?{|key|
        params[key] == @@default_values[key]
      }
      if is_pipe
        generate_pipe params, children_content
      else
        branch_parameters = [:branchtype, :branchmergemode].map {|key|
          params[key].to_sym
        }
        generate_branch branch_parameters, params, children_content
      end
    end
  end
# It generates the string for a pipe. If the type is a
# *SimpleTransformer* a `pipe` call is generated. Otherwise we use the
# *Language* method name for the class.
  def self.generate_pipe params, children_content
    result = ""
    result << ((self != SimpleTransformer) && language_method_name || "pipe")
    unless params.empty? || self == SimpleTransformer
      result << "(" << as_named_arguments(params) << ")"
    end
    result << " {"
    result  << children_content.call(" | ") << "}"
    result
  end
# It generates the string for a branch. If the type is a
# *SimpleTransformer* a `branch` call is generated. Otherwise we use
# the *Language* method name for the class.
  def self.generate_branch branch_parameters, params, children_content
    result = ""
    result << ((self != SimpleTransformer) && language_method_name || "branch")
    result << "(#{branch_parameters[0].inspect}, #{branch_parameters[1].inspect}"
    unless params.empty? || self == SimpleTransformer
      result << "," << as_named_arguments(params)
    end
    result << ") {\n  "
    result << children_content.call("\n  ")
    result << "\n}"
    result
  end

  def self.extract_params_from_element element
# We extract the params that come from the transformer element attributes.
    params = params_from_xml_attributes element
# Each child param element have a key attribute and its value as text content.
    children_list(element).select_with_name("param").each do |param_element|
      key = param_element.getAttribute('key').to_sym
      value = param_element.getTextContent.to_s.strip
# If the parameter element value is different than the default we
# assign it to the params.
      unless omitting_would_produce_same_value?(key, value)
        params[key] = value
      end
    end
    params
  end

# The parameters that come from the attributes are the ones defined in
# `@@default_values`: `:branchtype` and `:branchmergemode`.
  def self.params_from_xml_attributes element
    params = {}
    @@default_values.each_pair do |key, default_value|
      attrValue = element.getAttribute(key.to_s)
# Only if the attribute value is different than the default value we
# assign it to the params. The loop attribute is ignored here too.
      unless are_equal?(default_value, attrValue)
        params[key] = attrValue if key != :loop
      end
    end
    params
  end
# It produces the same value if it has the same value as the default
# value for the key.
  def self.omitting_would_produce_same_value? key, value
    default_value = with_defaults({})[key]
    are_equal?(default_value, value)
  end

  def self.are_equal?(value, dom_value)
    dom_value == value || dom_value == value.to_s
  end

# We strip the braces from the params hash. As the call only has one
# argument, we can provide the hash as named parameters. If params are
# empty only the method name is needed.
  def self.as_call params
    result = "#{language_method_name}"
    unless params.empty?
      result << "("
      result << as_named_arguments(params)
      result << ")"
    end
    result
  end

  def self.as_named_arguments params
    /{(.*)}/.match(params.inspect)[1]
  end
end

# Factory method for easily generating the *NodeList* for an element.
def children_list element
  NodeList.new(element.getChildNodes)
end

# *NodeList* is a wrapper around a java NodeList that makes some
# operations less verbose.
class NodeList
# We enrich this class with a lot of useful methods by including
# Enumerable.
  include Enumerable

  def initialize node_list
    @node_list = node_list
  end

# We expose the wrapped object.
  attr_reader :node_list

# Needed method for supporting Enumerable module.
  def each
    (0...node_list.getLength()).each do |i|
      yield node_list.item(i)
    end
  end
# Return all children with the provided name.
  def select_with_name name
    select{|n| n.java_kind_of?(XML::Element) && name == n.nodeName}
  end
end
