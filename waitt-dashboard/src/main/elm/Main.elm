module Main exposing(main)

import Api
import Browser exposing (Document)
import Browser.Navigation as Nav
import Json.Decode as Decode exposing (Value)
import Page exposing (Page)
import Page.Home as Home
import Page.Blank as Blank
import Page.NotFound as NotFound
import Page.Heap as Heap
import Page.Thread as Thread
import Page.Env as Env
import Page.Server as Server
import Html exposing(..)
import Route exposing (Route)
import Url exposing (Url)

type Model
    = Redirect Nav.Key
    | NotFound Nav.Key
    | Home Nav.Key Home.Model
    | Heap Nav.Key Heap.Model
    | Thread Nav.Key Thread.Model
    | Env Nav.Key Env.Model
    | Server Nav.Key Server.Model

-- MODEL

init : Url -> Nav.Key -> ( Model, Cmd Msg )
init url navKey =
    changeRouteTo (Route.fromUrl url)
        (Redirect navKey)

-- VIEW

view : Model -> Document Msg
view model =
    let
        viewPage page toMsg config =
            let { title, body } =
                    Page.view page config
            in
                { title = title
                , body = List.map (Html.map toMsg) body
                }
    in
        case model of
            Redirect _ ->
                Page.view Page.Other Blank.view
            NotFound _ ->
                Page.view Page.Other NotFound.view
            Home _ home ->
                viewPage Page.Home GotHomeMsg (Home.view home)
            Heap _ heap ->
                viewPage Page.Other GotHeapMsg (Heap.view heap)
            Thread _ thread ->
                viewPage Page.Other GotThreadMsg (Thread.view thread)
            Env _ env ->
                viewPage Page.Other GotEnvMsg (Env.view env)
            Server _ server ->
                viewPage Page.Other GotServerMsg (Server.view server)

-- UPDATE

type Msg
    = Ignored
    | ChangedRoute (Maybe Route)
    | ChangedUrl Url
    | ClickedLink Browser.UrlRequest
    | GotHomeMsg Home.Msg
    | GotHeapMsg Heap.Msg
    | GotThreadMsg Thread.Msg
    | GotEnvMsg Env.Msg
    | GotServerMsg Server.Msg

toNavKey : Model -> Nav.Key
toNavKey page =
    case page of
        Redirect key -> key
        NotFound key -> key
        Home key _   -> key
        Heap key _   -> key
        Thread key _   -> key
        Env key _   -> key
        Server key _   -> key

changeRouteTo : Maybe Route -> Model -> ( Model, Cmd Msg )
changeRouteTo maybeRoute model =
    case maybeRoute of
        Nothing ->
            ( NotFound (toNavKey model), Cmd.none )

        Just Route.Root ->
            ( model, Route.replaceUrl (toNavKey model) Route.Home)

        Just Route.Home ->
            Home.init
                |> updateWith (Home (toNavKey model)) GotHomeMsg model

        Just Route.ThreadDump ->
            Thread.init
                |> updateWith (Thread (toNavKey model)) GotThreadMsg model

        Just Route.HeapDump   ->
            Heap.init
                |> updateWith (Heap (toNavKey model)) GotHeapMsg model

        Just Route.Environments ->
            Env.init
                |> updateWith (Env (toNavKey model)) GotEnvMsg model

        Just Route.Server ->
            Server.init
                |> updateWith (Server (toNavKey model)) GotServerMsg model


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case ( msg, model ) of
        ( Ignored, _ ) ->
            ( model, Cmd.none )

        ( ClickedLink urlRequest, _ ) ->
            case urlRequest of
                Browser.Internal url ->
                    case url.fragment of
                        Nothing ->
                            ( model, Cmd.none )
                        Just _ ->
                            ( model
                            , Nav.pushUrl (toNavKey model) (Url.toString url)
                            )
                Browser.External href ->
                    ( model
                    , Nav.load href
                    )
        ( ChangedUrl url, _ ) ->
            changeRouteTo (Route.fromUrl url) model

        ( ChangedRoute route, _ ) ->
            changeRouteTo route model

        ( GotHomeMsg subMsg, Home key home) ->
            Home.update subMsg home
                |> updateWith (Home key) GotHomeMsg model

        ( GotHeapMsg subMsg, Heap key heap) ->
            Heap.update subMsg heap
                |> updateWith (Heap key) GotHeapMsg model

        ( GotThreadMsg subMsg, Thread key thread) ->
            Thread.update subMsg thread
                |> updateWith (Thread key) GotThreadMsg model

        ( GotEnvMsg subMsg, Env key env) ->
            Env.update subMsg env
                |> updateWith (Env key) GotEnvMsg model

        ( GotServerMsg subMsg, Server key server) ->
            Server.update subMsg server
                |> updateWith (Server key) GotServerMsg model

        ( _, _ ) ->
            ( model, Cmd.none )

updateWith : (subModel -> Model) -> (subMsg -> Msg) -> Model -> ( subModel, Cmd subMsg ) -> ( Model, Cmd Msg )
updateWith toModel toMsg model ( subModel, subCmd ) =
    ( toModel subModel
    , Cmd.map toMsg subCmd
    )

-- SUBSCRIPTIONS

subscriptions : Model -> Sub Msg
subscriptions model =
    case model of
        NotFound _ ->
            Sub.none

        Redirect _ ->
            Sub.none

        _ ->
            Sub.none

-- MAIN

main : Program Value Model Msg
main =
    let
        initApp flags url navKey =
            init url navKey
    in
    Browser.application
        { init = initApp
        , view = view
        , update = update
        , subscriptions = subscriptions
        , onUrlChange = ChangedUrl
        , onUrlRequest = ClickedLink
        }
