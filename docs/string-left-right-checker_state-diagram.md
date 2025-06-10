The String Left/Right Sequence Checker
=============

The Left/Right Sequence checker is a state machine that processes a string to check for specific left and right sequences. The state machine starts by checking the left sequence, then continues to the right sequence if the left sequence matches. If the right sequence does not match, it ends the process.

The state machine is designed to handle surrounding spaces and allows for a sequence of characters to be matched in the string. The diagram below illustrates the flow of the state machine.

```plantuml
@startuml
title String Left/Right Sequence Checker State Machine

state LeftSequenceChoice <<choice>>
[*] --> StartString


StartString --> StartString: bypass Surrounding Spaces
StartString --> LeftSequenceChoice
LeftSequenceChoice -> LeftSequenceMatch: match first char
LeftSequenceMatch --> ContinueSequenceMatch
ContinueSequenceMatch --> StartOfValue: sequence match complete
ContinueSequenceMatch -> ContinueSequenceMatch: next char
LeftSequenceChoice ---> StartOfValue: unmatch

StartOfValue --> ContinueOfValue
ContinueOfValue -> ContinueOfValue: char in set

state RightSequenceChoice <<choice>>
ContinueOfValue --> RightSequenceChoice
RightSequenceChoice -> RightSequenceMatch
RightSequenceMatch --> ContinueRightSequence
ContinueRightSequence -> ContinueRightSequence
ContinueRightSequence --> EndOfTemplate
RightSequenceChoice ---> EndOfTemplate: no right
EndOfTemplate -> EndOfTemplate: bypass Surrounding Spaces
EndOfTemplate --> [*]

@enduml
```