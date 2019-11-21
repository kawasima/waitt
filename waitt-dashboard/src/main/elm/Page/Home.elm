module Page.Home exposing (Model, Msg, init, update, view)

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

import Model.Application as Application exposing (Application)

-- MODEL

type alias Model =
    { application : Status Application
    }

type Status a
    = Loading
    | Loaded a
    | Failed

init : ( Model, Cmd Msg )
init =
    ( { application = Loading
      }
    , Task.attempt CompletedApplicationLoad fetchApplication
    )

-- VIEW

view : Model -> { title : String, content : Html Msg }
view model =
    { title = "WAITT dashboard"
    , content =
        case model.application of
            Loading -> div [ ] [ text "loading..." ]
            Loaded application -> viewApplication application
            Failed  -> div [ ] [ text "loading failed" ]
    }

viewApplication : Application -> Html Msg
viewApplication application =
    div [ class "three-forths column" ]
        [ h1 [ ]
              [ i [ class "meta-octicon octico-dashboard" ] [ ]
              , text "Application"
              ]
        , div [ ]
              [ table [ class "ui celled table" ]
                [ tbody [ ]
                  [ tr [ ]
                    [ th [ ]
                      [ text "Application Name" ]
                    , td [ ]
                      [ text application.configuration.applicationName ]
                    ]
                  , tr [ ]
                    [ th [ ]
                      [ text "Package" ]
                    , td [ ]
                      [ text (String.join "," application.configuration.packages) ]
                    ]
                  , tr [ ]
                    [ th [ ]
                      [ text "Base directory" ]
                    , td [ ]
                      [ text application.configuration.baseDirectory.path ]
                    ]
                  , tr [ ]
                    [ th [ ]
                      [ text "Source directory" ]
                    , td [ ]
                      [ text application.configuration.sourceDirectory.path ]
                    ]
                  ]
                ]
              ]
        ]

-- UPDATE

type Msg
    = CompletedApplicationLoad (Result Http.Error Application)

update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    let
        _ = Debug.log "info" model
    in
    case msg of
        CompletedApplicationLoad (Ok application) ->
            ( { model | application = Loaded application }, Cmd.none )
        CompletedApplicationLoad (Err error) ->
            let
                dummy = Debug.log "error" error
            in
            ( { model | application = Failed }
            , Log.error
            )

-- HTTP

fetchApplication : Task Http.Error Application
fetchApplication =
    let
        decoder =
            Application.decoder
        query = [ ]
    in
        Http.task
            { method = "GET"
            , headers = Api.headers
            , url = Api.url ["application"] query
            , body = Http.emptyBody
            , resolver = Api.jsonResolver decoder
            , timeout = Nothing
            }

-- SUBSCRIPTION

subscriptions : Model -> Sub Msg
subscriptions model =
    let
        dummy = Debug.log "info" "sub"
    in
    Sub.none


-- EXPORT
{-
toSession : Model -> Session
toSession model =
    model.session
-}
