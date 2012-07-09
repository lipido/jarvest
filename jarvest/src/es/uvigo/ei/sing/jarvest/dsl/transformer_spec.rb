# -*- coding: utf-8 -*-
#Author: Óscar González Fernández

require 'transformer'

describe "jarvest minilanguage" do

  describe "Transformer" do

    it "should accept description" do
      t = SimpleTransformer.new :description => "blabla"
      t.description.should == "blabla"
    end

    it "description shouldn't be allowed to be changed once set" do
      t = SimpleTransformer.new :description => "blabla"
      lambda{t.description = "blabla"}.should raise_error
    end

    it "should have transformer_class name as default value for description" do
      t = SimpleTransformer.new({})
      t.description.should == "SimpleTransformer"
    end

    it "should accept branchtype parameter" do
      t = SimpleTransformer.new :branchtype => "BRANCH_DUPLICATED"
      t.branchtype.should == :BRANCH_DUPLICATED
    end

    it "should have branchtype CASCADE as default" do
      t = SimpleTransformer.new({})
      t.branchtype.should == :CASCADE
    end

    it "should accept parameter branchmergemode" do
      t = SimpleTransformer.new :branchmergemode =>:ORDERED
      t.branchmergemode.should == :ORDERED
    end

    it "should have branchmergemode SCATTERED as default" do
      t = SimpleTransformer.new({})
      t.branchmergemode.should == :SCATTERED
    end

    it "should accept parameter loop" do
      t = SimpleTransformer.new :loop => true
      t.loop?.should be_true
    end

    it "should keep parameters" do
      t = SimpleTransformer.new :other => "bla"
      t.params[:other].should_not be_nil
    end

    it "should let add children" do
      t = SimpleTransformer.new :loop => true
      child = SimpleTransformer.new :loop => true
      t.add_child child
      t.children.should == [child]
    end

    it "must have an empty list as children initially" do
      t = SimpleTransformer.new :loop => true
      t.children.should_not be_nil
    end

    it "should not let modify original children collection" do
      t = SimpleTransformer.new :loop => true
      t.add_child(SimpleTransformer.new( :loop => true))
      t.children.clear
      t.children.length.should == 1
    end

    shared_examples_for "invariants for a Transformer" do
      it "should have a not nil description" do
        @transformer.description.should_not be_nil
      end

      it "should have a not nil transformer_class" do
        @transformer.transformer_class.should_not be_nil
      end

      it "should have a not nil branchtype" do
        @transformer.branchtype.should_not be_nil
      end

      it "should have a not nil branchmergemode" do
        @transformer.branchmergemode.should_not be_nil
      end

      def mock_children(transformer, mock = mock("child serializer"))
        attributes_mock = mock("attributes")
        mock.stub!(:attributes).and_return attributes_mock
        attributes_mock.should_receive("[]=").with('class', @transformer.transformer_class.to_s)
        attributes_mock.should_receive("[]=").
          with('branchtype', @transformer.branchtype.to_s)
        attributes_mock.should_receive("[]=").
          with('branchmergemode', @transformer.branchmergemode.to_s)
        attributes_mock.should_receive("[]=").with('loop', @transformer.loop?.to_s)
        pos = -1
        mock.should_receive("add_element").exactly(transformer.children.length).and_return{
                                             pos = pos + 1
                                             mock_children(transformer.children[pos])
                                            }
        mock.should_receive("add_param").exactly(transformer.params.size)
        mock
      end

      it "should traverse tranformer" do
        mock = mock("top serializer")
        @transformer.fill_transformer_element mock_children(@transformer, mock)
      end
    end

    describe "can have other transformers as children" do
      before(:all) do
        @transformer = PatternMatcher.new :pattern => 'bla'
        @child = PatternMatcher.new :pattern => %q{<li><a href="noticia.asp\?idnoticia=.*?">(.*?)</a>},
                                   :dotAll => true
        @transformer.add_child @child
        doc = @transformer.convert_to_xml
        doc.should_not be_nil
        # doc.write($stdout,4)
      end

      it_should_behave_like "invariants for a Transformer"

      it "should report the added children" do
        @transformer.children.size.should == 1
        @transformer.children[0].should == @child
      end
    end

    describe PatternMatcher do

      before(:all) do
        @transformer = PatternMatcher.new :pattern => 'bla'
      end

      it_should_behave_like "invariants for a Transformer"

      it "should have a fixed transformer class" do
        p = PatternMatcher.new :pattern => 'bla'
        p.transformer_class.should == :PatternMatcher
      end

      it "should require parameter pattern" do
        lambda {
          p = PatternMatcher.new({})
        }.should raise_error(ArgumentError, "pattern parameter is required")
      end

      it "should read parameter pattern" do
        p = PatternMatcher.new :pattern =>  'bla'
        p.pattern.should == 'bla'
      end

      it "should let specify pattern as just one param" do
        p = PatternMatcher.new('bla')
        p.pattern.should == 'bla'
      end

      it "should let specify additional params" do
        p = PatternMatcher.new :pattern => 'bla', :bla => "a"
        p.params[:bla].should == "a"
      end

    end
  end

  describe Language do
    it "should let put transformers in cascade" do
      result = Language.interpret { wget > match(:pattern => "bla") >
        replace(:sourceRE => "e", :dest => "a")}
    end

    it "should let the language be supplied as a string" do
      result = Language.language_eval %q{wget > match(:pattern => "bla") >
        replace(:sourceRE => "e", :dest => "a")}
      result.should_not be_nil
      result.transformer_class.should == :SimpleTransformer
      result.branchtype.should be_equal(:CASCADE)
      result.branchmergemode.should be_equal(:SCATTERED)
      result.children.size.should == 3
      first_wget = result.children[0]
      pattern_matcher = result.children[1]
      replacer = result.children[2]
      first_wget.class.should == URLRetriever
      pattern_matcher.class.should == PatternMatcher
      replacer.class.should == Replacer
    end

    it "should let put transformers as children of other transformer in cascade" do
      result = Language.interpret { wget {match(:pattern => "bla") >
        replace(:sourceRE => "e", :dest => "a")}}
      result.should_not be_nil
      result.transformer_class.should == :SimpleTransformer
      result.branchtype.should be_equal(:CASCADE)
      result.branchmergemode.should be_equal(:SCATTERED)
      result.children.size.should == 1
      wget = result.children[0]
      wget.children.size.should == 2
      wget.children[0].class.should == PatternMatcher
      wget.children[1].class.should == Replacer
    end

    it "should let put several consecutive pipes" do
      result = Language.interpret{ pipe {wget > match(:pattern => "bla")} |
        pipe {wget > match(:pattern => "bla")}}
      result.should_not be_nil
      result.children.size.should == 2
      first_pipe = result.children[0]
      first_pipe.transformer_class.should == :SimpleTransformer
      first_pipe.children.size.should == 2
      second_pipe = result.children[1]
      second_pipe.transformer_class.should == :SimpleTransformer
      second_pipe.children.size.should == 2
    end

    it "should let put transformers in branch" do
      result = Language.interpret { wget > branch(:BRANCH_DUPLICATED,:SCATTERED) {
          match(:pattern => "bla")
          match(:pattern => "eoo") } > append(:append => "bla")}
      result.should_not be_nil
      result.children.size.should == 3
      branch = result.children[1]
      branch.transformer_class.should == :SimpleTransformer
      branch.branchtype.should == :BRANCH_DUPLICATED
      branch.branchmergemode.should == :SCATTERED
      branch.children.size.should == 2
      branch.children[0].should respond_to(:pattern)
      branch.children[0].pattern.should == "bla"
      branch.children[1].should respond_to(:pattern)
      branch.children[1].pattern.should == "eoo"
    end

    it "should let put transformers in branch as children of other transformer" do
      result = Language.interpret { wget(:BRANCH_DUPLICATED,:SCATTERED) {
          match(:pattern => "bla")
          match(:pattern => "eoo") } > append(:append => "bla")}
      result.should_not be_nil
      result.children.size.should == 2
      wget = result.children[0]
      wget.transformer_class.should == :URLRetriever
      wget.branchtype.should == :BRANCH_DUPLICATED
      wget.branchmergemode.should == :SCATTERED
      wget.children.size.should == 2
      wget.children[0].should respond_to(:pattern)
      wget.children[0].pattern.should == "bla"
      wget.children[1].should respond_to(:pattern)
      wget.children[1].pattern.should == "eoo"
      append = result.children[1]
      append.transformer_class.should == :Appender
    end

    it "should not let use | inside a branch" do
      lambda do Language.interpret { wget > branch(:BRANCH_DUPLICATED,:SCATTERED) {
          match(:pattern => "bla") | match(:pattern => "eoo") }
      }
      end.should raise_error(RuntimeError, "| can't be used in a pipe branch")
    end

    it "should let use | after a branch" do
      result = Language.interpret { wget > branch(:BRANCH_DUPLICATED,:SCATTERED) {
          match(:pattern => "foo")
          match(:pattern => "bar")} | append(:append => :bar)
      }
      result.children.size.should == 3
      result.children[2].transformer_class.should == :Appender
    end

    it "should let put transformers in cascade inside branch" do
      result = Language.interpret { wget > branch(:BRANCH_DUPLICATED,:SCATTERED) {
          match(:pattern => "bla")
          match(:pattern => "eoo")
          pipe{wget > match(:pattern => "bla")}
        } > append(:append => "bla")}
      result.should_not be_nil
      result.children.size.should == 3
      result.children[1].children.size.should == 3
      pipes = result.children[1].children[2]
      pipes.children.size.should == 2
    end

    it "should let do a loop" do
      result = Language.interpret { wget |
        pipe{ match(:pattern => "bar") | wget}.repeat?{
          match(:pattern => "foo")
        } | append(:append => "bar")
      }
      result.should_not be_nil
      result.children.size.should == 3
      pipe = result.children[1]
      pipe.loop?.should be_true
      pipe.children.size.should == 3
      pipe.children[0].transformer_class.should == :PatternMatcher
      pipe.children[0].pattern.should == "foo"

      result.children[2].children.size.should == 0
      result.children[2].transformer_class.should == :Appender
    end

    it "should let use repeat? with any kind, not only containers" do
      result = Language.interpret { wget(:BRANCH_DUPLICATED,:SCATTERED) {
          append(:append => "append")
        }.repeat? {
          match(:pattern => "inside-repeate")
        }}
      result.children.size.should == 1
      wget = result.children[0]
      wget.loop?.should be_true
      wget.children.size.should == 2
      wget.children[0].transformer_class.should == :PatternMatcher
    end

    it "shouldn't allow to use repeat if no transformer defined at that level" do
      lambda {Language.interpret { wget |
          pipe{self.repeat? {
              match(:pattern => "foo")
            }} | append(:append => "bar")
        }
      }.should raise_error
    end

    it "should let put several transformer serially connected in the repeat clause" do
      result = Language.interpret { wget |
        pipe{ match(:pattern => "bla") | wget}.repeat?{
          match(:pattern => "prueba") > wget
        } | append(:append => "bla")
      }
      result.should_not be_nil
      result.children.size.should == 3
      result.children[1].children.size.should == 3
      result.children[1].children[0].transformer_class.should == :SimpleTransformer
      result.children[1].children[0].children.size.should == 2
    end

    it "should let put several transformers paralelly connected in the repeat clause" do
      result = Language.interpret { wget |
        pipe{ match(:pattern => "bla") | wget}.repeat?(:BRANCH_DUPLICATED,
                                                               :SCATTERED){
          match(:pattern => "prueba")
          wget
        } | append(:append => "bla")
      }
      result.should_not be_nil
      result.children.size.should == 3
      result.children[1].children.size.should == 3
      repeat_clause = result.children[1].children[0]
      repeat_clause.transformer_class.should == :SimpleTransformer
      repeat_clause.children.size.should == 2
      repeat_clause.branchtype.should == :BRANCH_DUPLICATED
      repeat_clause.branchmergemode.should == :SCATTERED
    end
  end
end
