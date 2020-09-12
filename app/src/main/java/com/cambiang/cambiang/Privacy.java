package com.cambiang.cambiang;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class Privacy extends AppCompatActivity {

    private String privacyTextContent;
    private String tcTextContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);

        init(); // Load privacy and T&C contents into variables

        TextView privacyText = (TextView) findViewById(R.id.privacy_text);
        TextView tcText = (TextView) findViewById(R.id.tc_text);

        privacyText.setText(privacyTextContent);
        tcText.setText(tcTextContent);
    }


    public void init()
    {

        privacyTextContent = "Política de Privacidade\n" +
                "A nossa equipa construiu o aplicativo Cambiang como um aplicativo gratuito. Este SERVIÇO é fornecido por nós sem custo e é destinado para uso tal como é.\n" +
                "\n" +
                "Esta página é usada para informar os visitantes sobre nossas políticas com a coleta, uso e divulgação de Informações Pessoais, se alguém decidir usar o nosso Serviço.\n" +
                "\n" +
                "Se você optar por usar o nosso Serviço, você concorda com a coleta e uso de informações em relação a esta política. As Informações Pessoais que nós coleciono são usadas para fornecer e melhorar o serviço. Não usarei nem compartilharei suas informações com ninguém, exceto conforme descrito nesta Política de Privacidade.\n" +
                "\n" +
                "Os termos usados \u200B\u200Bnesta Política de Privacidade têm os mesmos significados que os nossos Termos e Condições, que é acessível nesta página mais abaixo, a menos que definido de outra forma em Política de Privacidade.\n" +
                "\n" +
                "Coleta e Uso de Informações\n" +
                "\n" +
                "Para uma melhor experiência, ao usar nosso Serviço, nós podemos exigir que você nos forneça informação pessoalmente identificável. As informações que nós solicitamos serão retidas no seu dispositivo e não serão coletadas por nós de nenhuma maneira.  \n" +
                "\n" +
                "O aplicativo usa serviços de terceiros que podem coletar informações usadas para identificar você.\n" +
                "\n" +
                "Vincular à política de privacidade de provedores de serviços de terceiros usados pelo aplicativo\n" +
                "\n" +
                "Google Play Services\n" +
                "Dados de registo\n" +
                "\n" +
                "Queremos informar que sempre que você usar nosso Serviço, em caso de erro no aplicativo eu coletar dados e informações (através de produtos de terceiros) em seu telefone chamado Log Data. Esses dados de registo podem incluir informações como o endereço IP do dispositivo, nome do dispositivo, versão do sistema operacional, a configuração do aplicativo ao utilizar nosso Serviço, a hora e a data do seu uso do Serviço e outras estatísticas.\n" +
                "\n" +
                "Cookies\n" +
                "\n" +
                "Os cookies são arquivos com uma pequena quantidade de dados que são normalmente usados como identificadores exclusivos anônimos. Estes são enviados para o seu navegador a partir dos sites que você visita e são armazenados na memória interna do seu dispositivo. memória.\n" +
                "\n" +
                "Este serviço não usa esses \"cookies\" explicitamente. No entanto, o aplicativo pode usar código de terceiros e bibliotecas que usam “cookies” para coletar informações e melhorar seus serviços. Você tem a opção de aceite ou recuse esses cookies e saiba quando um cookie está sendo enviado para o seu dispositivo. Se você escolher Para recusar nossos cookies, você pode não conseguir usar algumas partes deste Serviço.\n" +
                "\n" +
                "Provedores de serviços\n" +
                "\n" +
                "Nós podemos empregar empresas e indivíduos de terceiros devido às seguintes razões:\n" +
                "\n" +
                "Facilitar nosso Serviço;\n" +
                "Para fornecer o Serviço em nosso nome;\n" +
                "Para realizar serviços relacionados ao Serviço; ou\n" +
                "Para nos ajudar a analisar como nosso Serviço é usado.\n" +
                "Queremos informar aos usuários deste Serviço que esses terceiros têm acesso a suas informações pessoais. O motivo é executar as tarefas atribuídas a eles em nosso nome. Contudo, eles são obrigados a não divulgar ou usar as informações para qualquer outra finalidade.\n" +
                "\n" +
                "Segurança\n" +
                "\n" +
                "Eu valorizo sua confiança em nos fornecer suas informações pessoais, por isso estamos nos esforçando usar meios comercialmente aceitáveis de protegê-lo. Mas lembre-se que nenhum método de transmissão a internet, ou o método de armazenamento eletrônico é 100% seguro e confiável, e nós não posso garantir sua segurança absoluta.\n" +
                "\n" +
                "Links para outros sites\n" +
                "\n" +
                "Este serviço pode conter links para outros sites. Se você clicar em um link de terceiros, você será direcionado para esses sites. Observe que esses sites externos não são operados por nós. Portanto, nós fortemente aconselhá-lo a rever a Política de Privacidade desses sites. Nós não tenho controlo sobre e não assumimos qualquer responsabilidade pelo conteúdo, políticas de privacidade ou práticas de quaisquer sites de terceiros ou serviços.\n" +
                "\n" +
                "Privacidade das crianças\n" +
                "\n" +
                "Estes Serviços não abordam ninguém com idade inferior a 13 anos. Não recolho intencionalmente informações pessoalmente identificáveis de crianças menores de 13 anos. No caso, descubrimos que uma criança menores de 13 anos nos forneceu informações pessoais, nós imediatamente apagamos isso de nossos servidores. Se você é pai ou responsável e sabe que seu filho nos forneceu informações pessoais informações, entre em contacto connosco para que nós possa fazer as ações necessárias.\n" +
                "\n" +
                "Alterações desta política de privacidade\n" +
                "\n" +
                "Nós podemos atualizar nossa Política de Privacidade de tempos em tempos. Assim, você é aconselhado a revisar esta página periodicamente para quaisquer alterações. Vamos notificá-lo de quaisquer alterações por postar a nova Política de Privacidade nesta página. Estas alterações entram em vigor imediatamente após serem publicadas nesta página.";

        tcTextContent = "Termos e Condições\n" +
                "Ao fazer o download ou usar o aplicativo, esses termos serão automaticamente aplicados a você - você deve se certificar portanto, que você os lê cuidadosamente antes de usar o aplicativo. Você não tem permissão para copiar ou modificar o app, qualquer parte do aplicativo ou nossas marcas registradas de qualquer forma. Você não tem permissão para tentar extrair o código-fonte do aplicativo, e você também não deve tentar traduzir o aplicativo para outros idiomas ou fazer versões derivadas. O aplicativo em si e todas as marcas registradas, direitos autorais, direitos de banco de dados e outros direitos de propriedade intelectual. direitos de propriedade relacionados a ele, ainda pertencem à Cambiang.\n" +
                "\n" +
                "A Cambiang está empenhada em garantir que o aplicativo seja o mais útil e eficiente possível. Para Por esse motivo, nos reservamos o direito de fazer alterações no aplicativo ou cobrar por seus serviços, a qualquer momento e por qualquer motivo. Nós nunca cobraremos pelo aplicativo ou pelos serviços sem deixar muito claro para você exatamente o que você está pagando.\n" +
                "\n" +
                "O aplicativo Cambiang armazena e processa dados pessoais que você nos forneceu, a fim de fornecer meu serviço. É sua responsabilidade manter seu telefone e acesso ao aplicativo seguro. Por isso Recomendamos que você não faça o jailbreak ou faça o root no seu telefone, que é o processo de remoção de restrições de software e limitações impostas pelo sistema operacional oficial do seu dispositivo. Poderia tornar seu telefone vulnerável a malware / vírus / programas maliciosos, comprometer os recursos de segurança do seu telefone e isso pode significar o aplicativo Cambiang não funcionará corretamente ou será exibido de maneira alguma.\n" +
                "\n" +
                "Você deve estar ciente de que há certas coisas que Cambiang não assumirá responsabilidade para. Certas funções do aplicativo exigirão que o aplicativo tenha uma conexão ativa com a Internet. A conexão pode ser Wi-Fi, ou fornecido pelo seu provedor de rede móvel, mas Cambiang não pode assumir a responsabilidade para o aplicativo não funcionar com todas as funcionalidades se você não tiver acesso ao Wi-Fi e não tiver do seu subsídio de dados à esquerda.\n" +
                "\n" +
                "Se você estiver usando o aplicativo fora de uma área com Wi-Fi, lembre-se de que seus termos do o contrato com seu provedor de rede móvel ainda será aplicado. Como resultado, você pode ser cobrado por sua operadora de telefonia móvel pelo custo dos dados pela duração da conexão enquanto acessa o aplicativo ou outros encargos de terceiros. Ao usar o aplicativo, você está aceitando a responsabilidade por qualquer um desses taxas, incluindo tarifas de dados em roaming, se você usar o aplicativo fora do território de origem (por exemplo, região ou país) sem desligar o roaming de dados. Se você não é o pagador de contas do dispositivo em que você está usando o aplicativo, esteja ciente de que supomos que você recebeu permissão do projeto pagador para usar o aplicativo.\n" +
                "\n" +
                "Na mesma linha, Cambiang nem sempre pode assumir a responsabilidade pela maneira como você usa o aplicativo, ou seja, você precisa se certificar de que o dispositivo permaneça carregado - se ficar sem bateria e você não pode ativá-lo, o Cambiang não pode aceitar a responsabilidade.\n" +
                "\n" +
                "Com relação à responsabilidade do Cambiang pelo uso do aplicativo, quando você estiver usando o aplicativo, é importante ter em mente que, embora nos esforcemos para garantir que ele seja atualizado e correta em todos os momentos, nós confiamos em terceiros para nos fornecer informações para que possamos fazer disponível para você. A Cambiang não aceita qualquer responsabilidade por qualquer perda, direta ou indireta, como resultado da confiança total nessa funcionalidade do aplicativo.\n" +
                "\n" +
                "Em algum momento, poderemos desejar atualizar o aplicativo. O aplicativo está atualmente disponível no Android - o requisitos para o sistema (e para quaisquer sistemas adicionais, decidimos estender a disponibilidade do aplicativo para) pode mudar, e você precisará fazer o download das atualizações se quiser continuar usando o aplicativo. Cambiang não promete que sempre atualizará o aplicativo para que seja relevante para você e / ou trabalha com a versão do Android que você instalou no seu dispositivo. Contudo, você promete sempre aceitar atualizações para o aplicativo quando oferecido a você, também podemos desejar pare de fornecer o aplicativo e pode rescindi-lo a qualquer momento sem dar aviso de rescisão para você. A menos que lhe seja dito o contrário, em qualquer rescisão, (a) os direitos e licenças concedidos a você nestes termos terminará; (b) você deve parar de usar o aplicativo e (se necessário) excluí-lo do seu dispositivo.\n" +
                "\n" +
                "Alterações nos Termos e Condições\n" +
                "\n" +
                "Nós podemos atualizar nossos Termos e Condições de tempos em tempos. Assim, você é aconselhado a revisar esta página periodicamente para quaisquer alterações. Vamos notificá-lo de quaisquer alterações por postar a nova Política de Privacidade nesta página. Estas alterações entram em vigor imediatamente após serem publicadas nesta página.\n" +
                "\n" +
                "Entre em contacto\n" +
                "\n" +
                "Se você tiver dúvidas ou sugestões sobre nossa Política de Privacidade bem como os nossos Termos e Condições, não hesite em contactar-nos.";

    }
}
