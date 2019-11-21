module Model.ThreadDump exposing (ThreadDump, ThreadInfo, StackTraceElement, decoder)

import Json.Decode as Decode exposing (Decoder)
import Json.Decode.Pipeline exposing (required, optional)

-- MODEL

type alias StackTraceElement =
    { declaringClass : String
    , methodName : String
    , fileName : String
    , lineNumber : Int
    }

type alias ThreadInfo =
    { threadId : Int
    , threadName : String
    , threadState : String
    , priority : Int
    , blockedTime : Int
    , blockedCount : Int
    , waitedTime : Int
    , waitedCount : Int
    , stackTrace : List StackTraceElement
    , suspended : Bool
    , daemon : Bool
    , inNative : Bool
    }
type alias ThreadDump =
    { threads : List ThreadInfo
    }

-- SERIALIZATION

decoderStackTraceElement : Decoder StackTraceElement
decoderStackTraceElement =
    Decode.succeed StackTraceElement
        |> required "declaringClass" Decode.string
        |> required "methodName" Decode.string
        |> required "fileName" Decode.string
        |> required "lineNumber" Decode.int

decoderThreadInfo : Decoder ThreadInfo
decoderThreadInfo =
    Decode.succeed ThreadInfo
        |> required "threadId" Decode.int
        |> required "threadName" Decode.string
        |> required "threadState" Decode.string
        |> required "priority" Decode.int
        |> required "blockedTime" Decode.int
        |> required "blockedCount" Decode.int
        |> required "waitedTime" Decode.int
        |> required "waitedCount" Decode.int
        |> required "stackTrace" (Decode.list decoderStackTraceElement)
        |> required "suspended" Decode.bool
        |> required "daemon" Decode.bool
        |> required "inNative" Decode.bool

decoder : Decoder ThreadDump
decoder =
    Decode.succeed ThreadDump
        |> required "threads" (Decode.list decoderThreadInfo)
