(import sk.fiit.dp.refactor.model.*)
(deftemplate JessInput (declare (from-class JessInput)))
(deftemplate JessOutput (declare (from-class JessOutput)))

(defrule empty-catch-clausule
    "Decision about refactoring for Empty Catch Clausule anti-pattern"
    ?o <-(JessInput {refCode == "ECC"})
    =>
    (add (new JessOutput ?o.code "LE")))
    
(defrule inapropriate-intimacy
    "Decision about refactoring for Inapropriate Intimacy code smell"
    ?o <-(JessInput {refCode == "II"})
    =>
    (add (new JessOutput ?o.code "MM"))
    (add (new JessOutput ?o.code "MF")))

(defrule long-parameter-list
    "Long parameter refactoring with possible collision check"
    ?o <-(JessInput {refCode == "LPL"})
    (JessInput (parents ?parentsList))
    (not(test (?parentsList contains "LM")))
    =>
    (add (new JessOutput ?o.code "IPO")))

(defrule catch-and-rethrow1
    "Catch and Rethrow refactoring decision with context checking"
    ?o <-(JessInput {refCode == "CR"})
    (JessInput {size > 1})
    =>
    (add (new JessOutput ?o.code "RET")))

(defrule catch-and-rethrow2
    "Catch and Rethrow refactoring decision with context checking"
    ?o <-(JessInput {refCode == "CR"})
    (JessInput {size == 1})
    =>
    (add (new JessOutput ?o.code "RTC")))

(defrule magic-numbers
    "Magic numbers refactoring decision"
    ?o <-(JessInput {refCode == "MAGIC"})
    =>
    (add (new JessOutput ?o.code "RMN")))
