module Page.Heap exposing (Model, Msg, init, update, view)

import Api
import Api.Endpoint as Endpoint
import Browser.Dom as Dom
import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (..)
import Http
import Page
import Route exposing (Route)
import Task exposing (Task)
import Log

import Model.HeapDump as HeapDump exposing (HeapDump, HeapHistogram)

-- MODEL

type alias Model =
    { heapDump : Status HeapDump
    }

type Status a
    = Loading
    | Loaded a
    | Failed

init : ( Model, Cmd Msg )
init =
    ( { heapDump = Loading
      }
    , Task.attempt CompletedHeapDumpLoad fetchHeapDump
    )

-- VIEW

view : Model -> { title : String, content : Html Msg }
view model =
    { title = "Heap dump / WAITT dashboard"
    , content =
        case model.heapDump of
            Loading -> div [ ] [ text "loading..." ]
            Loaded heapDump -> viewHeapDump heapDump
            Failed  -> div [ ] [ text "loading failed" ]
    }

viewHeapDump : HeapDump -> Html Msg
viewHeapDump heapDump =
    div [ class "three-forths column" ]
        [ h1 [ ]
              [ i [ class "cil-memory" ] [ ]
              , text "Heap Dump"
              ]
        , div [ ]
              [ table [ class "table" ]
                [ thead []
                  [ tr []
                    [ th [ scope "col" ] [ text "Class Name" ]
                    , th [ scope "col" ] [ text "Instance count" ]
                    , th [ scope "col" ] [ text "Total size" ]
                    ]
                  ]
                , tbody [ ]
                    (List.map viewHistogram heapDump.heapdump)
                ]
              ]
        ]

viewHistogram : HeapHistogram -> Html Msg
viewHistogram histogram =
    tr []
        [ td [ ] [ text histogram.className ]
        , td [ class "text-right" ] [ text (String.fromInt histogram.instanceCount) ]
        , td [ class "text-right" ] [ text (String.fromInt histogram.totalSize) ]
        ]

-- UPDATE

type Msg
    = CompletedHeapDumpLoad (Result Http.Error HeapDump)

update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        CompletedHeapDumpLoad (Ok heapDump) ->
            ( { model | heapDump = Loaded heapDump }, Cmd.none )
        CompletedHeapDumpLoad (Err error) ->
            ( { model | heapDump = Failed }
            , Log.error
            )

-- HTTP

fetchHeapDump : Task Http.Error HeapDump
fetchHeapDump =
    let
        decoder =
            HeapDump.decoder
        query = [ ]
    in
        Http.task
            { method = "GET"
            , headers = Api.headers
            , url = Api.url ["heap"] query
            , body = Http.emptyBody
            , resolver = Api.jsonResolver decoder
            , timeout = Nothing
            }

-- SUBSCRIPTION

subscriptions : Model -> Sub Msg
subscriptions model =
    Sub.none
